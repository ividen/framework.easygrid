package ru.kwanza.easygrid.map.impl;

/**
 * @author Alexander Guzanov
 */
public interface EntryReference<K, V> {

    public boolean isKeyEqual(Object key, int hash);

    public boolean isValueEqual(Object value);

    public V value();

    public K key();

    public int hash();

    public void setValue(V value);

    public boolean isValid();
}
