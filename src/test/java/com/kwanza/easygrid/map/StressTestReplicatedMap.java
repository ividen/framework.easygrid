package com.kwanza.easygrid.map;

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

import ru.kwanza.easygrid.map.impl.distributed.Type;
import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.MapBuilder;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
public class StressTestReplicatedMap {

    public static final class Entry implements Serializable {
        private byte[] buffer;

        public Entry(byte[] buffer) {
            this.buffer = buffer;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String mode = args[0];
        Type type = "sync".equals(args[1]) ? Type.SYNC : Type.ASYNC;
        String config = args[2];
        final long count = Long.valueOf(args[3]);
        int size = 0;
        if ("write".equals(mode)) {
            size = Integer.valueOf(args[4]);
            Map<String, Entry> map = MapBuilder.ttl(5, TimeUnit.HOURS)
                    .distributed("StressTestReplicatedMap")
                    .replicationType(type)
                    .configurator(config)
                    .newMap();
            byte[] testBuffer = new byte[size];
            long ts = System.currentTimeMillis();
            for (long i = 0; i < count; i++) {
                map.put(String.valueOf(i), new Entry(testBuffer));
            }

            System.out.println("Time: " + (System.currentTimeMillis() - ts));
        } else {
            Map<String, Entry> map = MapBuilder.ttl(5, TimeUnit.HOURS)
                    .distributed("StressTestReplicatedMap")
                    .replicationType(type)
                    .configurator(config)
                    .mapNotifier(new IMapObserver() {
                        AtomicLong counter = new AtomicLong(0);
                        long ts;

                        public void notifyPut(Object o, Object o1) {
                            long l = counter.incrementAndGet();
                            if (l == 1) {
                                ts = System.currentTimeMillis();
                            } else if (l == count) {
                                System.out.println("Time :" + (System.currentTimeMillis() - ts));
                            }
                        }

                        public void notifyRemove(Object o) {

                        }

                        public void notifyClearAll() {

                        }

                        public void notifyUpdate(Map.Entry entry) {

                        }
                    })
                    .newMap();
        }

        Thread.currentThread().join();

    }
}
