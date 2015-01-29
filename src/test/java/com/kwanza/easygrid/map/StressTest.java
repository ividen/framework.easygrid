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

import com.google.common.collect.MapMaker;
import ru.kwanza.easygrid.map.impl.HashFunction;
import javolution.util.FastMap;
import ru.kwanza.easygrid.map.MapBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
public class StressTest {
    private int threadCount;
    private int iteractionConut;
    private static AtomicLong s;
    private final Map<Long, Long> map;
    private String type;
    private static CyclicBarrier runAll;

    public StressTest(String type, int threadCount, int iteractionConut, Map<Long, Long> map) {
        this.threadCount = threadCount;
        this.iteractionConut = iteractionConut;
        this.type = type;
        s = new AtomicLong(threadCount);
        runAll = new CyclicBarrier(threadCount);
        this.map = map;
    }

    public static class TestRunnable implements Runnable {
        private int num;
        private long count;
        private Map<Long, Long> map;

        public TestRunnable(int num, long count, Map<Long, Long> map) {
            this.num = num;
            this.count = count;
            this.map = map;
        }

        public void run() {
            Random rnd = new Random(10000);
            try {
                runAll.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < count; i++) {
                long key = rnd.nextInt(100000);
                Long aLong = map.get(key);
                if (aLong == null) {
                    map.put(key, key);
                } else if (key % 100 == 0) {
                    map.remove(key);
                }
//                if (i % 1000000 == 0) {
//                    System.out.println(i);
//                }
            }
            s.decrementAndGet();
        }
    }

    public void run() throws InterruptedException {
        int COUNT = 5;
        int sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += doOneCycle();
        }

        System.out.println("------------ " + type + " (" + threadCount + "*" + iteractionConut + ")-----------");
        System.out.println("             Time=" + (sum / COUNT));
        System.gc();
    }

    private long doOneCycle() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("Tests");
        for (int i = 0; i < threadCount; i++) {
            new Thread(group, new TestRunnable(i, iteractionConut, map), "TestWrite -" + i).start();
        }

        long l = System.currentTimeMillis();
        while (s.get() > 0) {
            Thread.sleep(10);
        }

        return System.currentTimeMillis() - l;
    }

    public static void main(String[] args) throws InterruptedException {
        int threadCount = Integer.valueOf(args[0]);
        int iterationCount = Integer.valueOf(args[1]);
        String type = args[2];
        if ("hashtable".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new Hashtable()).run();
        } else if ("synchronizedMap".equals(type)) {
            new StressTest(type, threadCount, iterationCount, Collections.synchronizedMap(new HashMap())).run();
        } else if ("concurrentMap".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new ConcurrentHashMap(100, 0.75f, threadCount * 3)).run();
        } else if ("fastmap".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new FastMap().shared()).run();
        } else if ("simpleConcurrentMap_1.5".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.simple().hash(HashFunction.v_1_5).concurrencyLevel(threadCount).<Long, Long>newMap())
                    .run();
        } else if ("simpleConcurrentMap_1.6".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.simple().hash(HashFunction.v_1_6).concurrencyLevel(threadCount).<Long, Long>newMap())
                    .run();
        } else if ("ttlConcurrentMap".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.ttl(Long.valueOf(args[3]),TimeUnit.MILLISECONDS).concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("tolConcurrentMap".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder. tol(Long.valueOf(args[3]),TimeUnit.MILLISECONDS, Long.valueOf(args[4]),TimeUnit.MILLISECONDS)
                            .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("simpleConcurrentMap_1.5_soft".equals(type)) {
            new StressTest(type, threadCount, iterationCount, MapBuilder.simple().hash(HashFunction.v_1_5).soft()
                    .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("simpleConcurrentMap_1.6_soft".equals(type)) {
            new StressTest(type, threadCount, iterationCount, MapBuilder.simple().hash(HashFunction.v_1_6).soft()
                    .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("ttlConcurrentMap_soft".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.ttl(Long.valueOf(args[3])).soft().concurrencyLevel(threadCount).<Long, Long>newMap())
                    .run();
        } else if ("tolConcurrentMap_soft".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.tol(Long.valueOf(args[3]), Long.valueOf(args[4])).soft()
                            .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("simpleConcurrentMap_1.5_weak".equals(type)) {
            new StressTest(type, threadCount, iterationCount, MapBuilder.simple().hash(HashFunction.v_1_5).weak()
                    .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("simpleConcurrentMap_1.6_weak".equals(type)) {
            new StressTest(type, threadCount, iterationCount, MapBuilder.simple().hash(HashFunction.v_1_6).weak()
                    .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("ttlConcurrentMap_weak".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.ttl(Long.valueOf(args[3])).weak().concurrencyLevel(threadCount).<Long, Long>newMap())
                    .run();
        } else if ("tolConcurrentMap_weak".equals(type)) {
            new StressTest(type, threadCount, iterationCount,
                    MapBuilder.tol(Long.valueOf(args[3]), Long.valueOf(args[4])).weak()
                            .concurrencyLevel(threadCount).<Long, Long>newMap()).run();
        } else if ("google".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new MapMaker().concurrencyLevel(threadCount)
                    .expiration(Long.valueOf(args[3]), TimeUnit.MILLISECONDS).<Long, Long>makeMap()).run();
        } else if ("google_soft".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new MapMaker().softValues().concurrencyLevel(threadCount)
                    .expiration(Long.valueOf(args[3]), TimeUnit.MILLISECONDS).<Long, Long>makeMap()).run();
        } else if ("google_weak".equals(type)) {
            new StressTest(type, threadCount, iterationCount, new MapMaker().weakValues().concurrencyLevel(threadCount)
                    .expiration(Long.valueOf(args[3]), TimeUnit.MILLISECONDS).<Long, Long>makeMap()).run();
        } else {
            throw new UnsupportedOperationException("Type " + type + " is not supported!");
        }

        System.exit(0);
    }
}