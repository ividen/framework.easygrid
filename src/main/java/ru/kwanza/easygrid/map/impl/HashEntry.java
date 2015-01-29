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

import java.io.*;
import java.util.Map;

/**
 * @author Alexander Guzanov
 */
public class HashEntry<K, V> implements Map.Entry<K, V>, Externalizable {
    public EntryReference<K, V> reference;
    public transient HashEntry<K, V> next;

    public HashEntry(EntryReference<K, V> reference, HashEntry<K, V> next) {
        this.reference = reference;
        this.next = next;
    }

    public HashEntry() {
    }

    public static <K, V> HashEntry<K, V>[] newArray(int i) {
        return new HashEntry[i];
    }

    public boolean isValid() {
        return reference.isValid();
    }

    public K getKey() {
        return reference.key();
    }

    public V getValue() {
        return reference.value();
    }

    public V setValue(V value) {
        V result = reference.value();
        reference.setValue(value);
        return result;
    }

    public String toString() {
        return "HashEntry{key=" + getKey() + ", value=" + getValue() + "}";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(reference.key());
        out.writeObject(reference.value());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        reference = (EntryReference<K, V>) EntryStrength
                .STRONG.reference((Serializable) in.readObject(), (Serializable) in.readObject(), 0, null);
    }
}
