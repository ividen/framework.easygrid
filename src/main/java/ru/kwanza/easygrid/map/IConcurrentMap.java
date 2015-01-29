package ru.kwanza.easygrid.map;

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
