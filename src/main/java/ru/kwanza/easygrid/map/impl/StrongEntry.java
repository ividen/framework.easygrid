package ru.kwanza.easygrid.map.impl;

/**
 * @author Alexander Guzanov
 */
class StrongEntry<K, V> implements EntryReference<K, V>{
    private K key;
    private volatile V value;
    private final int hash;

    StrongEntry(K key, V value, int hash) {
        this.key = key;
        this.value = value;
        this.hash = hash;
    }

    public boolean isKeyEqual(Object key, int hash) {
        return this.hash == hash && key.equals(this.key);
    }

    public boolean isValueEqual(Object value) {
        if (value == null && this.value != null) {
            return false;
        } else if (value == null && this.value == null) {
            return true;
        }
        return value.equals(this.value);
    }

    public V value() {
        return value;
    }

    public K key() {
        return key;
    }

    public int hash() {
        return hash;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isValid() {
        return true;
    }
}
