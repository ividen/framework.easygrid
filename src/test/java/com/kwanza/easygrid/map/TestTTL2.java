package com.kwanza.easygrid.map;

import ru.kwanza.easygrid.map.MapBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TestTTL2 {
    public static void main(String args[]) {
        Map<String, String> map = MapBuilder.ttl(10000000L, TimeUnit.MINUTES).newMap();
        map.put("Test", "tesT");
        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println(e);
        }
    }
}
