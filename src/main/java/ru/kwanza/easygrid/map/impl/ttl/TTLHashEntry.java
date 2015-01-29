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
