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
import it.sauronsoftware.cron4j.Scheduler;
import org.jgroups.ChannelException;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Guzanov
 */
public class StressTestReplicatedLock {
    private static Scheduler scheduler = new Scheduler();

    private static final class TestReplicatedLockRunnable implements Runnable {
        private int iterationCount;
        static Lock[] locks = new Lock[10];
        static DistributedLockManager manager;

        static {
            try {
                manager = new DistributedLockManager("Test");
            } catch (ChannelException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            for (int j = 0; j < locks.length; j++) {
                locks[j] = manager.getLock("Test" + j);
            }

        }

        private TestReplicatedLockRunnable(int iterationCount) {
            this.iterationCount = iterationCount;
        }

        public void run() {
            System.out.println("BEGIN!!!");
            long ts = System.currentTimeMillis();
            for (int i = 0; i < iterationCount; i++) {
                for (int j = 0; j < locks.length; j++) {
                    locks[j].lock();
                }
                try {
//                    System.out.println(i);
                } finally {
                    for (int j = 0; j < locks.length; j++) {
                        locks[j].unlock();
                    }
                }
            }
            System.out.println("FINISHED !!! TS=" + (System.currentTimeMillis() - ts));
//            System.out.println("Lock=" + lock.toString());
//            DistributedLock.closeLock("Test");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String type = args[0];
        String schedulePattern = args[1];
        Integer iterationCount = Integer.valueOf(args[2]);

        Runnable run = "lock".equals(type)
                ? new TestReplicatedLockRunnable(iterationCount) : null;

        if (run == null) {
            throw new UnsupportedOperationException();
        }
        scheduler.start();
        scheduler.schedule(schedulePattern, run);

//        new Thread(run).start();

        Thread.currentThread().join();
    }
}
