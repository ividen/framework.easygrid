package ru.kwanza.easygrid.map.impl;

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

import java.io.Serializable;

/**
 * @author Alexander Guzanov
 */
public abstract class HashFunction implements Serializable {

    public static final HashFunction v_1_5 = new Hash_1_5();

    public static final HashFunction v_1_6 = new Hash_1_6();

    public abstract int hash(int h);

    public abstract String getName();

    private static class Hash_1_5 extends HashFunction {
        public int hash(int h) {
            h += ~(h << 9);
            h ^= (h >>> 14);
            h += (h << 4);
            h ^= (h >>> 10);
            return h;
        }

        @Override
        public String getName() {
            return "1_5";
        }
    }

    private static class Hash_1_6 extends HashFunction {
        public int hash(int h) {
            h += (h << 15) ^ 0xffffcd7d;
            h ^= (h >>> 10);
            h += (h << 3);
            h ^= (h >>> 6);
            h += (h << 2) + (h << 14);
            return h ^ (h >>> 16);
        }

        @Override
        public String getName() {
            return "1_6";
        }
    }
}
