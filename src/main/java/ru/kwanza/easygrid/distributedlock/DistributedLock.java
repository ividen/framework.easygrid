package ru.kwanza.easygrid.distributedlock;

import org.jgroups.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Experimental
 *
 * @author Alexander Guzanov
 */
public class DistributedLock implements Lock {
    private static final Logger logger = LoggerFactory.getLogger(DistributedLock.class);
    private static final long TIME_OUT = 1000;

    private final ReentrantLock lock = new ReentrantLock();
    private ReentrantLock coordinationLock;
    private Condition wakeUpCondition = lock.newCondition();
    private LinkedBlockingQueue<Address> lockQueue = new LinkedBlockingQueue<Address>();

    private String lockName;
    private DistributedLockManager manager;
    private volatile Address currentLockAddress;
    private volatile boolean repeatDistributedLock = false;

    DistributedLock(DistributedLockManager manager, String lockName) {
        this.lockName = lockName;
        this.manager = manager;
            this.coordinationLock = new ReentrantLock();
    }

    public String getLockName() {
        return lockName;
    }

    public void lock() {
        if (!lock.isHeldByCurrentThread()) {
            lock.lock();
            repeatDistributedLock = false;
            int count = 0;
            while (true) {
                try {
                    if (count > 0) {
                        logger.info("ReSend distributed lock");
                    }
                    manager.distributedLock(lockName);
                } catch (InterruptedException e) {
                    continue;
                }
                count++;
                try {
                    wakeUpCondition.await();
                } catch (InterruptedException e) {
                    continue;
                }
                if (!repeatDistributedLock) {
                    break;
                } else {
                    logger.warn("Was changed coordinator under lock! New coordinator {}. "
                            + "Acquire lock was retransmitted in cluster DistributedLock({})",
                            manager.getCoordinatorAddress(), lockName);
                    repeatDistributedLock = false;
                }
            }
        } else {
            lock.lock();
        }
    }

    public void unlock() {
        if (lock.getHoldCount() > 1) {
            lock.unlock();
            return;
        }
        manager.distributedUnlock(lockName);
        lock.unlock();
    }

    void handle(LockCommand command) {
        if (command.getType() == null) {
            return;
        }

        if (command.getType() == LockType.LOCK_COMMAND) {
            coordinationLock.lock();
            try {
                lockQueue.offer(command.getAddress());
                if (currentLockAddress == null) {
                    currentLockAddress = command.getAddress();
                    manager.wakeUpNode(lockName, currentLockAddress);
                }

            } finally {
                coordinationLock.unlock();
            }

        } else if (command.getType() == LockType.RELEASE_COMMAND) {
            coordinationLock.lock();
            try {
                if (command.getAddress().equals(currentLockAddress)) {
                    lockQueue.remove(currentLockAddress);
                    currentLockAddress = lockQueue.peek();
                    if (currentLockAddress != null) {
                        manager.wakeUpNode(lockName, currentLockAddress);
                    }
                } else {
                    if (!lockQueue.remove(command.getAddress())) {
                        logger.warn("Trying release lock for Node {} in cluster RelicatedLock({}) "
                                + "that is not in lockQueue!", command.getAddress(), lockName);
                    }
                }
            } finally {
                coordinationLock.unlock();
            }

        } else if (command.getType() == LockType.WAKE_UP_COMMAND) {
            lock.lock();
            try {
                wakeUpCondition.signalAll();
            } finally {
                lock.unlock();
            }
        } else {
            throw new UnsupportedOperationException();
        }

    }

    public void lockInterruptibly() throws InterruptedException {
        if (!lock.isHeldByCurrentThread()) {
            lock.lockInterruptibly();
            repeatDistributedLock = false;
            while (true) {
                manager.distributedLock(lockName);
                wakeUpCondition.await();
                if (!repeatDistributedLock) {
                    break;
                } else {
                    logger.warn("Was changed coordinator under lock! New coordinator {}. "
                            + "Acquire lock was retransmitted in cluster DistributedLock({})",
                            manager.getCoordinatorAddress(), lockName);
                    repeatDistributedLock = false;
                }
            }
        } else {
            lock.lockInterruptibly();
        }
    }

    public boolean tryLock() {
        try {
            return tryLock(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupting tryLock!", e);
            return false;
        }
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean result = true;
        if (!lock.isHeldByCurrentThread()) {
            long ts = System.nanoTime();

            result = lock.tryLock(time, unit);
            if (!result) {
                return false;
            }
            ts = unit.toNanos(time) - (System.nanoTime() - ts);
            if (ts <= 0) {
                lock.unlock();
                return false;
            }

            repeatDistributedLock = false;
            while (true) {
                try {
                    manager.distributedLock(lockName);
                } catch (InterruptedException e) {
                    lock.unlock();
                    throw e;
                }
                try {
                    result = wakeUpCondition.await(ts, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    lock.unlock();
                    throw e;
                }
                if (!result) {
                    manager.distributedUnlock(lockName);
                    lock.unlock();
                    break;
                }

                if (!repeatDistributedLock) {
                    break;
                } else {
                    logger.warn("Was changed coordinator under lock! New coordinator {}. "
                            + "Acquire lock was retransmitted in cluster DistributedLock({})",
                            manager.getCoordinatorAddress(), lockName);
                    repeatDistributedLock = false;
                }
            }
        } else {
            lock.lock();
        }
        return result;
    }

    public Condition newCondition() {
        throw new UnsupportedOperationException("Condition object is not supported by DistributedLock!");
    }

    void changeCoordinator(boolean isCoordinator) {
        lock.lock();
        try {
            logger.warn("Coordinator changed for {}. Mark for redistibure lock {} for  DistributedLock({})}",
                    new Object[]{manager.getCoordinatorAddress(), lockName, manager.getChannelName()});
            repeatDistributedLock = true;
            wakeUpCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    void suspectMember(Address suspected_mbr) {
        coordinationLock.lock();
        try {
            if (!lockQueue.remove(suspected_mbr) || currentLockAddress.equals(suspected_mbr)) {
                logger.warn("Node {} was wakedUP already in DistributedLock({})! Trying release lock manual!",
                        suspected_mbr, lockName);
                manager.distributedUnlock(lockName, suspected_mbr);
            } else {
                logger.warn("Lock Node {} deleted from queue in cluster DistributedLock({})!", suspected_mbr, lockName);
            }
        } finally {
            coordinationLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "DistributedLock{lockName='" + lockName + '\'' + ", coordinatorAddress=" + manager.getCoordinatorAddress()
                + ", currentLockAddress=" + currentLockAddress + '}';
    }

}
