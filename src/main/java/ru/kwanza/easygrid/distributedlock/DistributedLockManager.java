package ru.kwanza.easygrid.distributedlock;

/*
 * #%L
 * easygrid
 * %%
 * Copyright (C) 2015 Kwanza
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ru.kwanza.easygrid.util.JGroupsUtil;
import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.conf.XmlConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Guzanov
 */
public class DistributedLockManager implements MembershipListener, MessageListener, RequestHandler {
    private final ConcurrentMap<String, DistributedLock> lockMap = new ConcurrentHashMap<String, DistributedLock>();

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockManager.class);
    private static final long TIME_OUT = 1000;

    private JChannel channel;
    private String channelName;
    private MessageDispatcher dispatcher;
    private volatile Address coordinatorAddress;
    private boolean closed = false;

    public DistributedLockManager(String channelName, ProtocolStackConfigurator config) throws ChannelException {
        this.channelName = channelName;
        this.channel = new JChannel(config);
        initChannel();
    }

    public DistributedLockManager(String channelName, InputStream is) throws ChannelException, IOException {
        this(channelName, XmlConfigurator.getInstance(is));
    }

    public DistributedLockManager(String channelName, String filePath) throws ChannelException, IOException {
        this(channelName, new FileInputStream(filePath));
    }

    public DistributedLockManager(String channelName, URL url) throws ChannelException, IOException {
        this(channelName, url.openStream());
    }

    public DistributedLockManager(String channelName) throws ChannelException, IOException {
        this(channelName, Thread.currentThread().getContextClassLoader().getResourceAsStream("tcp.xml"));
    }

    public Address getCoordinatorAddress() {
        return coordinatorAddress;
    }

    private void initChannel() throws ChannelException {
        channel.connect(DistributedLock.class.getSimpleName() + "_" + channelName);
        coordinatorAddress = channel.getView().getVid().getCoordAddress();
        printViewInfo(channel.getView());
        dispatcher = new MessageDispatcher(channel, this, this, this);
    }

    public Lock getLock(String lockName) {
        DistributedLock lock = lockMap.get(lockName);
        if (lock == null) {
            lock = new DistributedLock(this, lockName);
            if (lockMap.putIfAbsent(lockName, lock) != null) {
               lock =  lockMap.get(lockName);
            }
        }

        return lock;
    }

    public void close() {
        closed = true;
        channel.disconnect();
    }

    private void checkClosed() {
        if (closed) {
            throw new UnsupportedOperationException("DistributedLockManager{" + channelName + "} is closed!");
        }
    }

    void distributedLock(String lockName) throws InterruptedException {
        checkClosed();
        dispatcher.castMessage(null,
                new Message(coordinatorAddress, null, new LockCommand(LockType.LOCK_COMMAND, lockName).asBytes()),
                GroupRequest.GET_NONE, TIME_OUT);
    }

    void distributedUnlock(String lockName) {
        checkClosed();
        distributedUnlock(lockName, null);
    }

    void distributedUnlock(String lockName, Address source) {
        checkClosed();
        dispatcher.castMessage(null,
                new Message(coordinatorAddress, source, new LockCommand(LockType.RELEASE_COMMAND, lockName).asBytes()),
                GroupRequest.GET_NONE, TIME_OUT);
    }

    void wakeUpNode(String lockName, Address wakeUpAddress) {
        checkClosed();
        dispatcher.castMessage(null,
                new Message(wakeUpAddress, null, new LockCommand(LockType.WAKE_UP_COMMAND, lockName).asBytes()),
                GroupRequest.GET_NONE, TIME_OUT);
    }

    public Object handle(Message msg) {
        LockCommand command = new LockCommand(msg.getSrc(), msg.getBuffer());

        if (logger.isTraceEnabled()) {
            logger.trace("Handle Command : type={}, lockName={}, node={}",
                    new Object[]{command.getType().name(), command.getLockName(), command.getAddress()});

        }

        DistributedLock distributedLock = (DistributedLock) getLock(command.getLockName());
        if (distributedLock != null) {
            distributedLock.handle(command);
        }

        return null;
    }

    boolean isCoordinator() {
        return channel.getLocalAddress().equals(coordinatorAddress);
    }

    private void printViewInfo(View view) {
        JGroupsUtil.printViewInfo(channel, DistributedLockManager.class, view);
    }

    public void viewAccepted(View new_view) {
        if (new_view instanceof MergeView) {
            logger.info("Merge some views in cluster DistributedLock({})", channelName);
            MergeView mergeView = (MergeView) new_view;
            StringBuffer buffer;
            buffer = new StringBuffer("Merged Views : \n ");
            for (View v : mergeView.getSubgroups()) {
                buffer.append(JGroupsUtil.getViewDumpInfo(channel, DistributedLockManager.class.getName(), v)).append('\n');
            }
            logger.info(buffer.toString());

        }
        printViewInfo(new_view);
        Address newCoordinatorAddress = new_view.getVid().getCoordAddress();
        if (!newCoordinatorAddress.equals(this.coordinatorAddress)) {
            logger.info("Coordinator was changed in cluster DistributedLock({})."
                    + " New coordinator address {}, old coordinator address {}.",
                    new Object[]{channelName, newCoordinatorAddress, this.coordinatorAddress});
            this.coordinatorAddress = newCoordinatorAddress;
            for (DistributedLock lock : lockMap.values()) {
                lock.changeCoordinator(isCoordinator());
            }
        }
    }

    public void suspect(Address suspected_mbr) {
        logger.warn("Node {} was disconnected from cluster DistributedLock({})!", suspected_mbr, channelName);
        if (!suspected_mbr.equals(coordinatorAddress) && !channel.getLocalAddress().equals(suspected_mbr)
                && isCoordinator() && channel.getView().containsMember(suspected_mbr)) {
            for (DistributedLock lock : lockMap.values()) {
                lock.suspectMember(suspected_mbr);
            }
        }
    }

    public void block() {
    }

    @Override
    public String toString() {
        return "DistributedLock{channelName='" + channelName + '\'' + ", coordinatorAddress=" + coordinatorAddress + '}';
    }

    public void receive(Message msg) {
    }

    public byte[] getState() {
        throw new UnsupportedOperationException(
                "STATE TRANSFERING is not supported in cluster " + channel.getClusterName());
    }

    public void setState(byte[] state) {
        throw new UnsupportedOperationException(
                "STATE TRANSFERING is not supported in cluster " + channel.getClusterName());
    }

    public String getChannelName() {
        return channelName;
    }
}
