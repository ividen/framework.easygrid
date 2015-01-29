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
