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

import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.MapBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TestTOL {
    private static final int ITERATION_COUNT = 1000;
    private static final int TTL_TIMEOUT = 20;
    private static final int TOL_TIMEOUT = 3;
    private static final TimeUnit TTL_TIMEUNIT = TimeUnit.SECONDS;
    private static final TimeUnit TOL_TIMEUNIT = TimeUnit.SECONDS;

    public static void main(String[] args) throws InterruptedException {

        IConcurrentMap<String, String> map =
                MapBuilder.tol(TTL_TIMEOUT, TTL_TIMEUNIT, TOL_TIMEOUT, TOL_TIMEUNIT).newMap();

        for (int i = 0; i < ITERATION_COUNT; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }

        System.out.println("-----BEGIN Must NOT EMPTY ------");
        long c = 0;
        long processedCount = 0;
        long ts = System.currentTimeMillis() + TTL_TIMEUNIT.toMillis(TTL_TIMEOUT + 1);
        long currentTs = System.currentTimeMillis();
        while (true) {
            if (ts < currentTs) {
                break;
            }
            long cycleCount = 0;
            for (int i = 0; i < ITERATION_COUNT; i++) {
                currentTs = System.currentTimeMillis();
                if (ts < currentTs) {
                    c += cycleCount;
                    break;

                }
                String key = String.valueOf(i);
                Object o = map.get(key);
                if (o != null) {
                    cycleCount++;
                } else {
                    System.out.println("Missed! " + key);
                }
            }
            processedCount += cycleCount;
            if (ts >= currentTs) {
                c += ITERATION_COUNT;
            }

        }

        System.out.println("ProcessedCount " + processedCount + " , estimatedCount " + c);
        System.out.println("-----END Must be NOT EMPTY ------");

        Thread.sleep(TOL_TIMEUNIT.toMillis(2 * TOL_TIMEOUT));

        System.out.println("-----BEGIN Must empty------");
        c = 0;
        for (int i = 0; i < ITERATION_COUNT; i++) {
            Object o = map.get(String.valueOf(i));
            if (o != null) {
                c++;
                System.out.println(o);
            }
        }
        System.out.println("Count c :" + c);
        System.out.println("-----BEGIN Must empty------");

    }
}
