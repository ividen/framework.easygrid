package com.kwanza.easygrid.map;

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
