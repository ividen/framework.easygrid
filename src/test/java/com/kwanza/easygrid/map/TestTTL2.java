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
