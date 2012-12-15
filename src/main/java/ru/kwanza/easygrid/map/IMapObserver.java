package ru.kwanza.easygrid.map;

import java.util.Map;

public interface IMapObserver<K, V> {
    void notifyPut(K k, V v);

    void notifyRemove(K k);

    void notifyClearAll();

    void notifyUpdate(Map.Entry<K, V> entry);
}
