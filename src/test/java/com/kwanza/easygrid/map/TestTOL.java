package com.kwanza.easygrid.map;

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
