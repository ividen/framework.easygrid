package com.kwanza.easygrid.map;

import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.MapBuilder;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Alexander Guzanov
 */
public class TestWeak {
    private static final int ITERATION_COUNT = 100;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        IConcurrentMap<String, String> map = MapBuilder.simple().weak().<String, String>newMap();

        for (int i = 0; i < ITERATION_COUNT; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }

        System.out.println("-----BEGIN ITERATE NOT EMPTY------");
        int c = 0;
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            c++;
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END ITERATE NOT EMPTY------");

        System.gc();
        System.gc();
        System.gc();
        System.gc();
        Thread.sleep(6000);

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        c = 0;
        System.out.println("-----BEGIN ITERATE  EMPTY------");
        iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            c++;
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END ITERATE  EMPTY ------");

    }

}
