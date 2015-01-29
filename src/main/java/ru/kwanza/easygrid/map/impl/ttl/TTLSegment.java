package ru.kwanza.easygrid.map.impl.ttl;

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

import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.impl.BaseSegment;
import ru.kwanza.easygrid.map.impl.EntryStrength;
import ru.kwanza.easygrid.map.impl.BaseSegment;
import ru.kwanza.easygrid.map.impl.EntryStrength;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class TTLSegment<K, V> extends BaseSegment<K, V, TTLHashEntry<K, V>> {
    private final long ttlTimeout;
    // Deque of time-to-live element, ordered by time
    // Before head - tail, after head - head
    private TTLHashEntry<K, V> head;

    public TTLSegment(EntryStrength strength, int initialCapacity, float lf, long ttlTimeout, TimeUnit ttlTimeUnit,
                      IMapObserver mapObserver) {
        super(strength, initialCapacity, lf, mapObserver);
        this.ttlTimeout = ttlTimeUnit.toMillis(ttlTimeout);
        head = new TTLHashEntry<K, V>(null, null, -1);
        head.ttlNext = head.ttlPrev = head;
    }

    public TTLSegment(EntryStrength strength, int initialCapacity, float lf, long ttlTimeout, TimeUnit ttlTimeUnit) {
        this(strength, initialCapacity, lf, ttlTimeout, ttlTimeUnit, null);
    }

    protected TTLHashEntry<K, V> createEntry(K key, int hash, TTLHashEntry<K, V> first, int index) {
        return new TTLHashEntry<K, V>(strength.<K, V>reference(key, null, hash, this), first,
                System.currentTimeMillis() + ttlTimeout);
    }

    @Override
    protected void updateEntryValue(TTLHashEntry<K, V> e, V newValue, boolean notify) {
        e.reference.setValue(newValue);
        e.ts = System.currentTimeMillis() + ttlTimeout;
        moveToTail(e);
        if (notify && mapObserver != null) {
            mapObserver.notifyUpdate(e);
        }
    }

    protected TTLHashEntry<K, V> copyHashEntry(TTLHashEntry<K, V> entry, TTLHashEntry<K, V> next) {
        TTLHashEntry<K, V> result =
                new TTLHashEntry<K, V>(entry.reference, next, entry.ts);
        result.ttlNext = entry.ttlNext;
        result.ttlPrev = entry.ttlPrev;

        entry.ttlPrev.ttlNext = result;
        entry.ttlNext.ttlPrev = result;

        return result;
    }

    @Override
    protected void removedEntry(TTLHashEntry<K, V> entry) {
        removeEntryFromList(entry);
    }

    private void removeEntryFromList(TTLHashEntry<K, V> entry) {
        entry.ttlPrev.ttlNext = entry.ttlNext;
        entry.ttlNext.ttlPrev = entry.ttlPrev;
        entry.ttlNext = entry.ttlPrev = null;
    }

    @Override
    public V replace(K key, int hash, V newValue, boolean broadcast) {
        lock();
        try {
            clearStaleBefore(System.currentTimeMillis());
            return super.replace(key, hash, newValue, broadcast);
        } finally {
            unlock();
        }
    }

    @Override
    public boolean replace(K key, int hash, V oldValue, V newValue, boolean broadcast) {
        lock();
        try {
            clearStaleBefore(System.currentTimeMillis());
            return super.replace(key, hash, oldValue, newValue, broadcast);
        } finally {
            unlock();
        }
    }

    @Override
    public V put(K key, int hash, V value, boolean onlyIfAbsent, boolean broadcast) {
        lock();
        try {
            clearStaleBefore(System.currentTimeMillis());
            return super.put(key, hash, value, onlyIfAbsent, broadcast);
        } finally {
            unlock();
        }
    }

    @Override
    public V remove(Object key, int hash, Object value, boolean broadcast) {
        lock();
        try {
            clearStaleBefore(System.currentTimeMillis());
            return super.remove(key, hash, value, broadcast);
        } finally {
            unlock();
        }
    }

    @Override
    public void clear() {
        lock();
        try {
            super.clear();
            TTLHashEntry<K, V> e = head.ttlNext;
            while (e != head) {
                TTLHashEntry<K, V> next = e.ttlNext;
                e.ttlNext = e.ttlPrev = null;
                e = next;
            }
            head.ttlNext = head.ttlPrev = head;

        } finally {
            unlock();
        }
    }

    void clearStaleBefore(long currentTs) {
        TTLHashEntry<K, V> element;
        while ((element = head.ttlNext) != head && element.ts < currentTs) {
            super.remove(element.reference.key(), element.reference.hash(), null, true);
        }
    }

    protected void moveToTail(TTLHashEntry<K, V> newEntry) {
        insertBefore(newEntry, head);
    }

    private void insertBefore(TTLHashEntry<K, V> newEntry, TTLHashEntry entry) {
        newEntry.ttlNext = entry;
        newEntry.ttlPrev = entry.ttlPrev;
        newEntry.ttlPrev.ttlNext = newEntry;
        newEntry.ttlNext.ttlPrev = newEntry;
    }

    @Override
    public TTLHashEntry<K, V> updateEntry(TTLHashEntry<K, V> e, int hash, boolean notify) {
        lock();
        try {
            clearStaleBefore(System.currentTimeMillis());
            final K key = e.getKey();
            if (count != 0) {
                TTLHashEntry<K, V> entry = getFirst(hash);
                while (entry != null) {
                    if (entry.reference.isKeyEqual(key, hash)) {
                        boolean updateEvent = false;
                        if (!entry.getValue().equals(e.getValue())) {
                            entry.setValue(e.getValue());
                            updateEvent = true;
                        }

                        final long ts = e.ts;
                        if (entry.ts != ts) {
                            updateEvent = true;
                            placeEntryInNewOrder(entry, ts);
                        }

                        if (updateEvent && notify && mapObserver != null) {
                            mapObserver.notifyUpdate(entry);
                        }

                        return entry;

                    }
                    entry = (TTLHashEntry<K, V>) entry.next;
                }
            }

        } finally {
            unlock();
        }

        return null;
    }

    protected void placeEntryInNewOrder(TTLHashEntry<K, V> entry, long newTimestamp) {
        removeEntryFromList(entry);
        TTLHashEntry<K, V> element = head.ttlNext;
        while (element != head && element.ts > newTimestamp) {
            element = element.ttlNext;
        }

        insertBefore(entry, element);
    }

}
