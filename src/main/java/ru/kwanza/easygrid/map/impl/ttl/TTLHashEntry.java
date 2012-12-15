package ru.kwanza.easygrid.map.impl.ttl;

import ru.kwanza.easygrid.map.impl.EntryReference;
import ru.kwanza.easygrid.map.impl.EntryStrength;
import ru.kwanza.easygrid.map.impl.HashEntry;

import java.io.*;

/**
 * @author Alexander Guzanov
 */
public class TTLHashEntry<K, V> extends HashEntry<K, V> implements Externalizable {
    public long ts;
    public transient TTLHashEntry<K, V> ttlNext;
    public transient TTLHashEntry<K, V> ttlPrev;

    public TTLHashEntry(EntryReference<K, V> reference, HashEntry<K, V> next, long ts) {
        super(reference, next);
        this.ts = ts;
    }

//    TTLHashEntry(EntryReference<K, V> reference, HashEntry<K, V> next, long ts, TTLHashEntry<K, V> ttlNext,
//                 TTLHashEntry<K, V> ttlPrev) {
//        this(reference, next, ts);
//        this.ttlNext = ttlNext;
//        this.ttlPrev = ttlPrev;
//    }

    public TTLHashEntry() {
    }

    public boolean isValid() {
        long ts = this.ts;
        return ts >= System.currentTimeMillis() && super.isValid();
    }

    @Override
    public String toString() {
        return "TTLHashEntry{key=" + getKey() + ", value=" + getValue() + ", ts =" + ts + "}";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(reference.key());
        out.writeObject(reference.value());
        out.writeLong(ts);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        reference = (EntryReference<K, V>) EntryStrength.STRONG
                .reference((Serializable) in.readObject(), (Serializable) in.readObject(), 0, null);
        ts = in.readLong();
    }
}
