package ru.kwanza.easygrid.map.impl.distributed;

import java.io.Serializable;

class PutMessage<K, V> implements Serializable {
    K key;
    V value;

    public PutMessage(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
