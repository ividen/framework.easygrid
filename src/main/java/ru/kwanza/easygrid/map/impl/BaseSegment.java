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

import ru.kwanza.easygrid.map.IMapObserver;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Guzanov
 */
public class BaseSegment<K, V, E extends HashEntry<K, V>> extends ReentrantLock {
    private int threshold;
    private final float loadFactor;
    protected final EntryStrength strength;
    public volatile HashEntry<K, V>[] table;
    public volatile int count;
    int modCount;
    protected IMapObserver mapObserver = null;

    public BaseSegment(EntryStrength strength, int initialCapacity, float lf, IMapObserver mapObserver) {
        loadFactor = lf;
        this.strength = strength;
        this.mapObserver = mapObserver;
        setTable(newArray(initialCapacity));
    }

    public BaseSegment(EntryStrength strength, int initialCapacity, float lf) {
        this(strength, initialCapacity, lf, null);
    }

    protected E createEntry(K key, int hash, E first, int index) {
        return (E) new HashEntry<K, V>(strength.<K, V>reference(key, null, hash, this), first);
    }

    protected V getEntryValue(E e) {
        if (e.isValid()) {
            V v = e.reference.value();
            if (v != null) {
                return v;
            }
            return readValueUnderLock(e);
        }

        return null;
    }

    protected void updateEntryValue(E e, V newValue, boolean notify) {
        e.reference.setValue(newValue);
        if (notify && mapObserver != null) {
            mapObserver.notifyUpdate(e);
        }
    }

    protected E copyHashEntry(E src, E next) {
        return (E) new HashEntry<K, V>(src.reference, next);
    }

    protected void removedEntry(E e) {
    }

    private E[] newArray(int initialCapacity) {
        return (E[]) new HashEntry[initialCapacity];
    }

    private void setTable(E[] newTable) {
        threshold = (int) (newTable.length * loadFactor);
        table = newTable;
    }

    protected E getFirst(int hash) {
        E[] tab = (E[]) table;
        return tab[hash & (tab.length - 1)];
    }

    private V readValueUnderLock(HashEntry<K, V> e) {
        lock();
        try {
            return e.reference.value();
        } finally {
            unlock();
        }
    }

    public V get(Object key, int hash) {
        if (count != 0) {
            HashEntry<K, V> e = getFirst(hash);
            while (e != null) {
                if (e.reference.isKeyEqual(key, hash)) {
                    return getEntryValue((E) e);
                }
                e = e.next;
            }
        }
        return null;
    }

    public boolean containsKey(Object key, int hash) {
        if (count != 0) {
            HashEntry<K, V> e = getFirst(hash);
            while (e != null) {
                if (e.reference.isKeyEqual(key, hash)) {
                    return e.isValid();
                }
                e = e.next;
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (count != 0) {
            HashEntry<K, V>[] tab = table;
            int len = tab.length;
            for (int i = 0; i < len; i++) {
                for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                    V v = e.reference.value();
                    if (v == null) {
                        v = readValueUnderLock(e);
                    }
                    if (value.equals(v)) {
                        return e.isValid();
                    }
                }
            }
        }
        return false;
    }

    public boolean replace(K key, int hash, V oldValue, V newValue, boolean broadcast) {
        lock();
        try {
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && !e.reference.isKeyEqual(key, hash)) {
                e = e.next;
            }

            boolean replaced = false;
            if (e != null && oldValue.equals(e.reference.value())) {
                if (e.isValid()) {
                    replaced = true;
                    updateEntryValue((E) e, newValue, broadcast);
                }
            }
            return replaced;
        } finally {
            unlock();
        }
    }

    public V replace(K key, int hash, V newValue, boolean broadcast) {
        lock();
        try {
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && !e.reference.isKeyEqual(key, hash)) {
                e = e.next;
            }

            V oldValue = null;
            if (e != null) {
                oldValue = e.reference.value();
                updateEntryValue((E) e, newValue, broadcast);
            }
            return oldValue;
        } finally {
            unlock();
        }
    }

    public V put(K key, int hash, V value, boolean onlyIfAbsent, boolean broadcast) {
        lock();
        try {
            int c = count;
            if (c++ > threshold) {
                rehash();
            }
            HashEntry<K, V>[] tab = table;
            int index = hash & (tab.length - 1);
            HashEntry<K, V> first = tab[index];
            HashEntry<K, V> e = first;
            while (e != null && !e.reference.isKeyEqual(key, hash)) {
                e = e.next;
            }

            V oldValue;
            if (e != null) {
                oldValue = e.reference.value();
                if (!onlyIfAbsent && !oldValue.equals(value)) {
                    updateEntryValue((E) e, value, broadcast);
                }
            } else {
                oldValue = null;
                ++modCount;
                E entry = createEntry(key, hash, (E) first, index);
                tab[index] = entry;
                updateEntryValue(entry, value, false);
                count = c; // write-volatile
                if (broadcast && mapObserver != null) {
                    mapObserver.notifyPut(key, value);
                }
            }
            return oldValue;
        } finally {
            unlock();
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    private void rehash() {
        HashEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= Constants.MAXIMUM_CAPACITY) {
            return;
        }

        HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
        threshold = (int) (newTable.length * loadFactor);
        int sizeMask = newTable.length - 1;
        for (int i = 0; i < oldCapacity; i++) {
            HashEntry<K, V> e = oldTable[i];

            if (e != null) {
                HashEntry<K, V> next = e.next;
                int idx = e.reference.hash() & sizeMask;

                if (next == null) {
                    newTable[idx] = e;
                } else {
                    HashEntry<K, V> lastRun = e;
                    int lastIdx = idx;
                    for (HashEntry<K, V> last = next; last != null; last = last.next) {
                        int k = last.reference.hash() & sizeMask;
                        if (k != lastIdx) {
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIdx] = lastRun;

                    for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                        int k = p.reference.hash() & sizeMask;
                        HashEntry<K, V> n = newTable[k];
                        newTable[k] = copyHashEntry((E) p, (E) n);
                    }
                }
            }
        }
        table = newTable;
    }

    public V remove(Object key, int hash, Object value, boolean broadcast) {
        lock();
        try {
            int c = count - 1;
            HashEntry<K, V>[] tab = table;
            int index = hash & (tab.length - 1);
            HashEntry<K, V> e = tab[index];
            HashEntry<K, V> prev = null;
            while (e != null && !e.reference.isKeyEqual(key, hash)) {
                prev = e;
                e = e.next;
            }

            V oldValue = null;
            if (e != null) {
                V v = e.reference.value();
                if (value == null || value.equals(v)) {
                    oldValue = v;
                    ++modCount;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    removedEntry((E) e);
                    count = c; // write-volatile
                }
                if (broadcast && mapObserver != null) {
                    mapObserver.notifyRemove((Serializable) key);
                }
            }
            return oldValue;
        } finally {
            unlock();
        }
    }

    void removeEntryReference(EntryReference<K, V> reference) {
        lock();
        try {
            int c = count - 1;
            HashEntry<K, V>[] tab = table;
            int index = reference.hash() & (tab.length - 1);
            HashEntry<K, V> e = tab[index];
            HashEntry<K, V> prev = null;
            while (e != null && e.reference != reference) {
                prev = e;
                e = e.next;
            }

            if (e != null) {
                V v = e.reference.value();
                ++modCount;
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                removedEntry((E) e);
                count = c; // write-volatile
            }
        } finally {
            unlock();
        }
    }

    public void clear() {
        if (count != 0) {
            lock();
            try {
                HashEntry<K, V>[] tab = table;
                for (int i = 0; i < tab.length; i++) {
                    tab[i] = null;
                }
                ++modCount;
                count = 0;
            } finally {
                unlock();
            }
        }
    }

    public void setMapLsitener(IMapObserver mapObserver) {
        this.mapObserver = mapObserver;
    }

    public E updateEntry(E e, int hash, boolean notify) {
        lock();
        try {
            final K key = e.getKey();
            if (count != 0) {
                HashEntry<K, V> entry = getFirst(hash);
                while (entry != null) {
                    if (entry.reference.isKeyEqual(key, hash)) {
                        if (e != entry) {
                            entry.setValue(e.getValue());
                            if (notify && mapObserver != null) {
                                mapObserver.notifyUpdate(e);
                            }
                        }
                        return (E) entry;
                    }
                    entry = entry.next;
                }
            }
        } finally {
            unlock();
        }

        return null;
    }

    public E getEntry(K key, int hash) {
        if (count != 0) {
            HashEntry<K, V> entry = getFirst(hash);
            while (entry != null) {
                if (entry.reference.isKeyEqual(key, hash)) {
                    return entry.isValid() ? (E) entry : null;
                }
                entry = entry.next;
            }
        }

        return null;
    }
}

