package ru.kwanza.easygrid.map.impl;

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
