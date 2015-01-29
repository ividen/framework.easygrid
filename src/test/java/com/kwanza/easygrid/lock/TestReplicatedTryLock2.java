package com.kwanza.easygrid.lock;

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
