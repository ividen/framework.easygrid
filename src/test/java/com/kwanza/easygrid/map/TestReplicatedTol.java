package com.kwanza.easygrid.map;

import ru.kwanza.easygrid.map.impl.distributed.Type;
import org.jgroups.ChannelException;
import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.MapBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TestReplicatedTol {

    public static void main(String[] args) throws IOException, ChannelException, InterruptedException {

        IMapObserver<String, String> mapObserver = new IMapObserver<String, String>() {

            public void notifyPut(String s, String s1) {
//                System.out.println("Put new item " + s + "=" + s1);
            }

            public void notifyRemove(String s) {
//                System.out.println("Remove item " + s);
            }

            public void notifyClearAll() {
//                System.out.println("Clear map");
            }

            public void notifyUpdate(Map.Entry<String, String> stringStringEntry) {
                System.out.println("Update entry " + stringStringEntry);
            }
        };

        IConcurrentMap<String, String> map = MapBuilder.ttl(30000000)
                .mapNotifier(mapObserver)
                .distributed(TestReplicatedTol.class.getSimpleName())
                .configurator("D:/tcp.xml")
                .replicationType(Type.SYNC)
                .jmx("pcentre-back", "WebscriptCache")
                .newMap();


        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }
        IConcurrentMap<Serializable, Serializable> concurrentMap =
                MapBuilder.simple().jmx("pcentre-back", "simple").newMap();

        for (int i = 0; i < 10000; i++) {
            concurrentMap.put(String.valueOf(i), String.valueOf(i));
        }

        IConcurrentMap<Serializable, Serializable> concurrentMap1 =
                MapBuilder.ttl(1000, TimeUnit.HOURS).jmx("pcentre-back", "ttl").newMap();

        IConcurrentMap<Serializable, Serializable> concurrentMap2 =
                MapBuilder.tol(1000, TimeUnit.HOURS, 100, TimeUnit.SECONDS)
                        .weak()
                        .concurrencyLevel(1000)
                        .jmx("pcentre-back", "tol")
                        .newMap();
        for (int i = 0; i < 10000; i++) {
            concurrentMap2.put(String.valueOf(i), String.valueOf(i));
        }

        for (int i = 0; i < 10000; i++) {
            concurrentMap1.put(String.valueOf(i), String.valueOf(i));
        }

//
        Thread.sleep(1000000000);
//
//        for (int i = 0; i < 100000000; i++) {
//            int i1 = new Random().nextInt(100);
//            map.get(String.valueOf(i1));
//        }
//
//        Thread.sleep(6000);
//        for (int i = 0; i < 100; i++) {
//            map.put(String.valueOf(i), String.valueOf(i));
//        }
//
//        System.out.println("SIZE = " + map.size());
//        System.out.println("FINISHED!!!");

    }

}
