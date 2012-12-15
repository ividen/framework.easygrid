package ru.kwanza.easygrid.map.impl.distributed;

import java.io.Serializable;

class RemoveMessage<K extends Serializable> implements Serializable {
    K key;

    public RemoveMessage(K k) {
        this.key = k;
    }

    public K getKey() {
        return key;
    }
}
