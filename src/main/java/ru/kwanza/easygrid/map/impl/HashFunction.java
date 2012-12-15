package ru.kwanza.easygrid.map.impl;

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
