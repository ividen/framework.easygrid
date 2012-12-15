package ru.kwanza.easygrid.map.impl.ttl;

import ru.kwanza.easygrid.map.impl.AbstractConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.IMapConfig;

/**
 * Time-To-Live concurrent HashMap
 *
 * @author Alexander Guzanov
 */
public class TTLConcurrentHashMap<K, V> extends AbstractConcurrentHashMap<K, V, TTLHashEntry<K, V>>
        implements TTLConcurrentHashMapMBean {
    public TTLConcurrentHashMap(MapConfigWithTTL config) {
        super(config);
    }

    protected class TTLHashIterator<K, V> extends HashIterator<K, V> {

        TTLHashIterator() {
            super();
        }

        @SuppressWarnings({"UnusedAssignment"})
        protected boolean advance() {
            long currentTs = System.currentTimeMillis();
            boolean result;
            while (true) {
                result = super.advance();
                TTLHashEntry<K, V> hashEntry = (TTLHashEntry<K, V>) nextEntry;
                if (hashEntry == null) {
                    return false;
                }
                long entryTs = hashEntry.ts;
                if (currentTs <= entryTs) {
                    break;
                }
            }

            return result;
        }
    }

    public int size() {
        long sum = 0;
        lockMap();
        try {
            long ts = System.currentTimeMillis();
            for (int i = 0; i < segments.length; i++) {
                TTLSegment<K, V> segment = (TTLSegment<K, V>) segments[i];
                segment.clearStaleBefore(ts);
                sum += segment.count;
            }
        } finally {
            unlockMap();
        }

        if (sum > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) sum;
        }
    }

    protected HashIterator<K, V> hashIterator() {
        return new TTLHashIterator<K, V>();
    }

    protected TTLSegment<K, V> allocateSegment(IMapConfig config, int cap) {
        MapConfigWithTTL configWithTTL = (MapConfigWithTTL) config;
        return new TTLSegment<K, V>(config.getEntryStrength(), cap, config.getLoadFactor(),
                configWithTTL.getTTLTimeout(), configWithTTL.getTTLTimeUnit());
    }

    public void shrink() {
        lockMap();
        try {
            long ts = System.currentTimeMillis();
            for (int i = 0; i < segments.length; i++) {
                TTLSegment<K, V> segment = (TTLSegment<K, V>) segments[i];
                segment.clearStaleBefore(ts);
            }
        } finally {
            unlockMap();
        }
    }
}
