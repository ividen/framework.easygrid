package com.kwanza.easygrid.map;

import org.jgroups.ChannelException;
import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.MapBuilder;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TestTTL {
    private static final int ITERATION_COUNT = 10000;

    public static void main(String[] args)
            throws InterruptedException, IOException, ClassNotFoundException, ChannelException {
        IConcurrentMap<String, String> map = MapBuilder.ttl(4, TimeUnit.SECONDS).<String, String>newMap();


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

        Thread.sleep(6000);

        c = 0;
        System.out.println("-----BEGIN ITERATE  EMPTY------");
        iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            c++;
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END ITERATE  EMPTY ------");

        System.out.println("-----BEGIN Must be empty ------");
        c = 0;
        for (int i = 0; i < ITERATION_COUNT; i++) {
            Object o = map.remove(String.valueOf(i));
            if (o != null) {
                c++;
            }
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END Must be empty ------");

        for (int i = 0; i < ITERATION_COUNT; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }

        System.out.println("-----BEGIN Must NOT EMPTY ------");
        c = 0;
        for (int i = 0; i < ITERATION_COUNT; i++) {
            Object o = map.get(String.valueOf(i));
            if (o != null) {
                c++;
            }
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END Must NOT EMPTY ------");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        IConcurrentMap o = (IConcurrentMap) ois.readObject();

        System.out.println("-----BEGIN ITERATE NOT EMPTY------");
        c = 0;
        iterator = o.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            c++;
        }
        System.out.println("Count c :" + c);
        System.out.println("-----END ITERATE NOT EMPTY------");
    }

}
