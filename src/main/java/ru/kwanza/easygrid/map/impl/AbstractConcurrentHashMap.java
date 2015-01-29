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

import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.IMapObserver;

import java.io.*;
import java.util.*;

/**
 * @author Alexander Guzanov
 */
public abstract class AbstractConcurrentHashMap<K, V, E extends HashEntry<K, V>>
        implements IConcurrentMap<K, V>, Serializable, AbstractConcurrentHashMapMBean {

    private final int segmentMask;
    private final int segmentShift;
    protected transient BaseSegment<K, V, E>[] segments;
    private final IMapConfig config;
    private transient HashFunction hashFunction;
    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, V>> entrySet;
    private transient Collection<V> values;
    private IMapObserver mapNotifier = null;

    protected AbstractConcurrentHashMap(IMapConfig config) {
        if (!(config.getLoadFactor() > 0) || config.getInitialCapacity() < 0 || config.getConcurrencyLevel() <= 0
                || config.getHashFunc() == null) {
            throw new IllegalArgumentException();
        }

        this.config = config;
        this.mapNotifier = config.getMapNotifier();

        int concurrencyLevel = config.getConcurrencyLevel();
        if (concurrencyLevel > Constants.MAX_SEGMENTS) {
            concurrencyLevel = Constants.MAX_SEGMENTS;
        }
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        initSegments(config);

        if (config.getJmxName() != null) {
            initJMX(config);
        }
    }

    private void initJMX(IMapConfig config) {
        JMXUtil.forcedRegister(this, config.getJmxName());
        JMXUtil.forcedRegister(this, config.getJmxName() + ", config=Config");
    }

    private void initSegments(IMapConfig config) {
        this.hashFunction = config.getHashFunc();
        int ssize = segmentMask + 1;
        this.segments = AbstractConcurrentHashMap.createSegmentArray(ssize);

        int initialCapacity = config.getInitialCapacity();
        if (initialCapacity > Constants.MAXIMUM_CAPACITY) {
            initialCapacity = Constants.MAXIMUM_CAPACITY;
        }
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity) {
            ++c;
        }
        int cap = 1;
        while (cap < c) {
            cap <<= 1;
        }

        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = allocateSegment(config, cap);
        }
    }

    private static <K, V, E extends HashEntry<K, V>> BaseSegment<K, V, E>[] createSegmentArray(int i) {
        return new BaseSegment[i];
    }

    private BaseSegment<K, V, E> segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    protected abstract BaseSegment<K, V, E> allocateSegment(IMapConfig config, int cap);

    public V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, true);
    }

    public V putIfAbsent(K key, V value, boolean notify) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).put(key, hash, value, true, notify);
    }

    public V remove(Object key) {
        return remove(key, true);
    }

    public boolean remove(Object key, Object value) {
        return remove(key, value, true);
    }

    public boolean remove(Object key, Object value, boolean notify) {
        int hash = hashFunction.hash(key.hashCode());
        if (value == null) {
            return false;
        }
        return segmentFor(hash).remove(key, hash, value, notify) != null;
    }

    public V remove(Object key, boolean broadcast) {
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).remove(key, hash, null, broadcast);
    }

    public boolean replace(K key, V oldValue, V newValue) {
        return replace(key, oldValue, newValue, true);
    }

    public boolean replace(K key, V oldValue, V newValue, boolean notify) {
        if (oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).replace(key, hash, oldValue, newValue, notify);
    }

    public V replace(K key, V value) {
        return replace(key, value, true);
    }

    public V replace(K key, V value, boolean notify) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).replace(key, hash, value, notify);
    }

    public int size() {
        final BaseSegment<K, V, E>[] segments = this.segments;
        long sum = 0;
        long check = 0;
        int[] mc = new int[segments.length];

        for (int k = 0; k < Constants.RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
            }
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    check += segments[i].count;
                    if (mc[i] != segments[i].modCount) {
                        check = -1; // force retry
                        break;
                    }
                }
            }
            if (check == sum) {
                break;
            }
        }
        if (check != sum) {
            sum = 0;
            lockMap();
            for (BaseSegment<K, V, E> segment : segments) {
                sum += segment.count;
            }
            unlockMap();
        }
        if (sum > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) sum;
        }
    }

    public void lockMap() {
        for (BaseSegment<K, V, E> segment : segments) {
            segment.lock();
        }
    }

    public boolean isEmpty() {
        final BaseSegment<K, V, E>[] segments = this.segments;

        int[] mc = new int[segments.length];
        int mcsum = 0;
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
                return false;
            } else {
                mcsum += mc[i] = segments[i].modCount;
            }
        }
        if (mcsum != 0) {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].count != 0 || mc[i] != segments[i].modCount) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean containsKey(Object key) {
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).containsKey(key, hash);
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final BaseSegment<K, V, E>[] segments = this.segments;
        int[] mc = new int[segments.length];

        for (int k = 0; k < Constants.RETRIES_BEFORE_LOCK; ++k) {
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                mcsum += mc[i] = segments[i].modCount;
                if (segments[i].containsValue(value)) {
                    return true;
                }
            }
            boolean cleanSweep = true;
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    if (mc[i] != segments[i].modCount) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep) {
                return false;
            }
        }

        lockMap();
        boolean found = false;
        try {
            for (BaseSegment<K, V, E> segment : segments) {
                if (segment.containsValue(value)) {
                    found = true;
                    break;
                }
            }
        } finally {
            unlockMap();
        }
        return found;
    }

    public void unlockMap() {
        for (BaseSegment<K, V, E> segment : segments) {
            segment.unlock();
        }
    }

    public V get(Object key) {
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).get(key, hash);
    }

    public V put(K key, V value) {
        return put(key, value, true);
    }

    public V put(K key, V value, boolean notify) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).put(key, hash, value, false, notify);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean notify) {
        for (BaseSegment<K, V, E> segment : segments) {
            segment.clear();
        }
        if (notify && mapNotifier != null) {
            mapNotifier.notifyClearAll();
        }
    }

    public IMapObserver<K, V> getMapNotifier() {
        return mapNotifier;
    }

    public void setMapNotifier(IMapObserver notifier) {
        this.mapNotifier = notifier;
        lockMap();
        try {
            for (BaseSegment<K, V, E> seg : segments) {
                seg.setMapLsitener(notifier);
            }
        } finally {
            unlockMap();
        }
    }

    public void updateEntry(E entry, boolean notify) {
        int hash = hashFunction.hash(entry.getKey().hashCode());
        segmentFor(hash).updateEntry(entry, hash, notify);
    }

    public HashEntry<K, V> getEntry(K key) {
        int hash = hashFunction.hash(key.hashCode());
        return segmentFor(hash).getEntry(key, hash);
    }

    protected class HashIterator<K, V> {
        private int nextSegmentIndex;
        private int nextTableIndex;
        private BaseSegment<K, V, ?> currentSegment;
        protected HashEntry<K, V> nextEntry;
        protected HashEntry<K, V> lastReturned;

        protected HashIterator() {
            nextSegmentIndex = segments.length - 1;
            nextTableIndex = -1;
            advance();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        protected boolean advance() {
            if (nextEntry != null && nextEntry.isValid() && (nextEntry = nextEntry.next) != null) {
                return true;
            }

            while (nextTableIndex >= 0) {
                if ((nextEntry = currentSegment.table[nextTableIndex--]) != null && nextEntry.isValid()) {
                    return true;
                }
            }

            while (nextSegmentIndex >= 0) {
                BaseSegment<K, V, ?> seg = (BaseSegment<K, V, ?>) segments[nextSegmentIndex--];
                if (seg.count != 0) {
                    currentSegment = seg;
                    for (int j = currentSegment.table.length - 1; j >= 0; --j) {
                        if ((nextEntry = currentSegment.table[j]) != null && nextEntry.isValid()) {
                            nextTableIndex = j - 1;
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public boolean hasNext() {
            return nextEntry != null;
        }

        HashEntry<K, V> nextEntry() {
            if (nextEntry == null) {
                throw new NoSuchElementException();
            }
            lastReturned = nextEntry;
            advance();
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            AbstractConcurrentHashMap.this.remove(lastReturned.reference.key());
            lastReturned = null;
        }
    }

    final class KeyIterator implements Iterator<K>, Enumeration<K> {
        private final HashIterator<K, V> hashIterator;

        KeyIterator(HashIterator<K, V> hashIterator) {
            this.hashIterator = hashIterator;
        }

        public boolean hasNext() {
            return hashIterator.hasNext();
        }

        public K next() {
            return hashIterator.nextEntry().reference.key();
        }

        public void remove() {
            hashIterator.remove();
        }

        public boolean hasMoreElements() {
            return hashIterator.hasMoreElements();
        }

        public K nextElement() {
            return hashIterator.nextEntry().reference.key();
        }
    }

    final class ValueIterator implements Iterator<V>, Enumeration<V> {
        private final HashIterator<K, V> hashIterator;

        ValueIterator(HashIterator<K, V> hashIterator) {
            this.hashIterator = hashIterator;
        }

        public boolean hasNext() {
            return hashIterator.hasNext();
        }

        public V next() {
            return hashIterator.nextEntry().reference.value();
        }

        public void remove() {
            hashIterator.hasMoreElements();
        }

        public boolean hasMoreElements() {
            return hashIterator.hasMoreElements();
        }

        public V nextElement() {
            return hashIterator.nextEntry().reference.value();
        }
    }

    final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator(hashIterator());
        }

        public int size() {
            return AbstractConcurrentHashMap.this.size();
        }

        public boolean contains(Object o) {
            return AbstractConcurrentHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return AbstractConcurrentHashMap.this.remove(o) != null;
        }

        public void clear() {
            AbstractConcurrentHashMap.this.clear();
        }
    }

    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator(hashIterator());
        }

        public int size() {
            return AbstractConcurrentHashMap.this.size();
        }

        public boolean contains(Object o) {
            return AbstractConcurrentHashMap.this.containsValue(o);
        }

        public void clear() {
            AbstractConcurrentHashMap.this.clear();
        }
    }

    final class WriteThroughEntry implements Entry<K, V> {
        private EntryReference<K, V> reference;

        WriteThroughEntry(EntryReference<K, V> reference) {
            this.reference = reference;
        }

        public K getKey() {
            return reference.key();
        }

        public V getValue() {
            return reference.value();
        }

        public V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }
            V v = reference.value();
            reference.setValue(value);
            AbstractConcurrentHashMap.this.put(getKey(), value);
            return v;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
        public Map.Entry<K, V> next() {
            HashEntry<K, V> e = super.nextEntry();
            return new WriteThroughEntry(e.reference);
        }
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = AbstractConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return AbstractConcurrentHashMap.this.remove(e.getKey(), e.getValue());
        }

        public int size() {
            return AbstractConcurrentHashMap.this.size();
        }

        public void clear() {
            AbstractConcurrentHashMap.this.clear();
        }
    }

    protected HashIterator<K, V> hashIterator() {
        return new HashIterator<K, V>();
    }

    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    public void copyToMap(Map<K, V> values) {
        for (BaseSegment<K, V, E> s : segments) {
            s.lock();
            try {
                HashEntry<K, V>[] tab = s.table;
                for (HashEntry<K, V> aTab : tab) {
                    for (HashEntry<K, V> e = aTab; e != null; e = e.next) {
                        EntryReference<K, V> reference = e.reference;
                        K key = reference.key();
                        V value = reference.value();
                        if (key != null && value != null) {
                            values.put(key, value);
                        }
                    }
                }
            } finally {
                s.unlock();
            }
        }
    }

    public int estimatedCount() {
        int result = 0;
        for (BaseSegment<K, V, E> s : segments) {
            result += s.count;
        }

        return result;
    }

    public void drainToMap(Map<K, V> values) {
        for (BaseSegment<K, V, E> s : segments) {
            s.lock();
            try {
                HashEntry<K, V>[] tab = s.table;
                for (HashEntry<K, V> aTab : tab) {
                    for (HashEntry<K, V> e = aTab; e != null; e = e.next) {
                        EntryReference<K, V> reference = e.reference;
                        K key = reference.key();
                        V value = reference.value();
                        if (key != null && value != null) {
                            values.put(key, value);
                        }
                    }
                }
            } finally {
                s.clear();
                s.unlock();

            }
        }

    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        lockMap();
        try {
            writeEntries(s);
        } finally {
            unlockMap();
        }
    }

    public void writeEntries(ObjectOutputStream s) throws IOException {
        for (BaseSegment<K, V, E> seg : segments) {
            HashEntry<K, V>[] tab = seg.table;
            for (HashEntry<K, V> aTab : tab) {
                for (HashEntry<K, V> e = aTab; e != null; e = e.next) {
                    EntryReference<K, V> reference = e.reference;
                    K key = reference.key();
                    V value = reference.value();
                    if (key != null && value != null) {
                        s.writeObject(key);
                        s.writeObject(value);
                        s.flush();
                    }
                }
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        initSegments(config);

        readEntries(s);
    }

    public void readEntries(ObjectInputStream s) throws IOException, ClassNotFoundException {
        for (; ;) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            if (key == null) {
                break;
            }
            put(key, value);
        }
    }

    public int getEstimatedCount() {
        return estimatedCount();
    }

    public void printContent() {
        printContent(System.out);
    }

    public void printContentToFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file, false);
        PrintStream stream = new PrintStream(fos);
        printContent(stream);
    }

    public void printContent(PrintStream writer) {
        for (BaseSegment<K, V, E> seg : segments) {
            seg.lock();
            try {
                HashEntry<K, V>[] tab = seg.table;
                for (HashEntry<K, V> aTab : tab) {
                    for (HashEntry<K, V> e = aTab; e != null; e = e.next) {
                        EntryReference<K, V> reference = e.reference;
                        K key = reference.key();
                        V value = reference.value();
                        if (key != null && value != null) {
                            writer.println(e);
                        }
                    }
                }
            } finally {
                seg.unlock();
            }
        }
    }

    public void saveToFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file, false);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public int getSegmentCount() {
        return segments.length;
    }
}
