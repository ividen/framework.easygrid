package com.kwanza.easygrid.lock;

import ru.kwanza.easygrid.distributedlock.DistributedLockManager;
import org.jgroups.ChannelException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Guzanov
 */
public class TestReplicatedTryLock2 {

    public static void main(String[] args) throws InterruptedException, ChannelException, IOException {
        DistributedLockManager manager = new DistributedLockManager("Test");
        Lock lock = manager.getLock("Test");
//        Lock lock = Hazelcast.getLock("Test");
        Thread.sleep(1000);
        RandomAccessFile randomAccessFile = new RandomAccessFile("test.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();

        FileLock fileLock = null;
        if (lock.tryLock(20015, TimeUnit.MILLISECONDS)) {
            System.out.println("In Lock!");
            try {
                fileLock = channel.tryLock();
                if (fileLock == null) {
                    throw new RuntimeException("Cant;t lock");
                }
                Thread.sleep(20000);
            } finally {
                if (fileLock != null) {
                    fileLock.release();
                }
                System.out.println("UnLock!");
                lock.unlock();

            }
        } else {
            System.out.println("Denyed Lock!");
        }
        randomAccessFile.close();
        Thread.currentThread().join();
    }
}
