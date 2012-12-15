package ru.kwanza.easygrid.map;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexander Guzanov
 */
public interface IConcurrentMap<K, V> extends ConcurrentMap<K, V> {

    V put(K key, V value, boolean notify);

    V putIfAbsent(K key, V value, boolean notify);

    boolean remove(Object key, Object value, boolean notify);

    V remove(Object key, boolean notify);

    boolean replace(K key, V oldValue, V newValue, boolean notify);

    V replace(K key, V oldValue, boolean notify);

    void clear(boolean notify);

    public void copyToMap(Map<K, V> values);

    public int estimatedCount();

    public void drainToMap(Map<K, V> values);

    public void lockMap();

    public void unlockMap();

    public void writeEntries(ObjectOutputStream os) throws IOException;

    public void readEntries(java.io.ObjectInputStream is) throws IOException, ClassNotFoundException;

    public void setMapNotifier(IMapObserver<K, V> mapObserver);

    public IMapObserver<K, V> getMapNotifier();

}
