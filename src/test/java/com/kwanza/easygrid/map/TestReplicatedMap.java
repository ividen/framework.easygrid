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
import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.MapBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TestReplicatedMap {

    public static final void main(String[] args) throws InterruptedException {
        IConcurrentMap<Object, Object> map = MapBuilder.ttl(10, TimeUnit.SECONDS)
                .concurrencyLevel(100)
                .distributed("TestMap")
                .configurator("c:\\java\\servers\\mts_payment\\conf\\tcp.xml")
                .mapNotifier(new IMapObserver() {
                    public void notifyPut(Object o, Object o1) {
                        System.out.println("Put " + o);
                    }

                    public void notifyRemove(Object o) {
                        System.out.println("Remove " + o);
                    }

                    public void notifyClearAll() {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void notifyUpdate(Map.Entry entry) {
                        System.out.println("Update " + entry.getKey() + " value=" + entry.getValue());
                    }
                })
                .newMap();

        for (int i = 0; i < 200; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }

        Thread.sleep(1000 * 20);
        for (int i = 0; i < 200; i++) {
            map.put(String.valueOf(i), String.valueOf(10));
        }
    }
}
