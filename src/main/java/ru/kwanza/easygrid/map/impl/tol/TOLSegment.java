package ru.kwanza.easygrid.map.impl.tol;

import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.impl.EntryStrength;
import ru.kwanza.easygrid.map.impl.ttl.TTLHashEntry;
import ru.kwanza.easygrid.map.impl.ttl.TTLSegment;
import ru.kwanza.easygrid.map.impl.EntryStrength;
import ru.kwanza.easygrid.map.impl.ttl.TTLHashEntry;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TOLSegment<K, V> extends TTLSegment<K, V> {
    private final long tolTimeout;

    public TOLSegment(EntryStrength strength, int initialCapacity, float lf, long ttlTimeout, TimeUnit ttlTimeUnit,
                      long tolTimeout, TimeUnit tolTimeUnit, IMapObserver mapNotifier) {
        super(strength, initialCapacity, lf, ttlTimeout, ttlTimeUnit, mapNotifier);
        this.tolTimeout = tolTimeUnit.toMillis(tolTimeout);
    }

    public TOLSegment(EntryStrength strength, int initialCapacity, float lf, long ttlTimeout, TimeUnit ttlTimeUnit,
                      long tolTimeout, TimeUnit tolTimeUnit) {
        this(strength, initialCapacity, lf, ttlTimeout, ttlTimeUnit, tolTimeout, tolTimeUnit, null);
    }

    protected V getEntryValue(TTLHashEntry<K, V> e) {
        V value = super.getEntryValue(e);
        if (value != null) {
            long deadLine = System.currentTimeMillis();
            long ts = e.ts;
            if (ts < deadLine) {
                lock();
                try {
                    ts = e.ts;
                    if (ts < deadLine) {
                        ts += tolTimeout;
                        e.ts = ts;

                        updateEntry(e, e.reference.hash(), true);
                    }
                } finally {
                    unlock();
                }
            }
        }
        return value;
    }

    protected void moveToTail(TTLHashEntry<K, V> newEntry) {
        lock();
        try {
            super.moveToTail(newEntry);
        } finally {
            unlock();
        }
    }

}
