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

import ru.kwanza.easygrid.map.IMapObserver;

/**
 * @author Alexander Guzanov
 */
public class MapConfig implements IMapConfig,MapConfigMBean {
    private int initialCapacity = Constants.DEFAULT_INITIAL_CAPACITY;
    private float loadFactor = Constants.DEFAULT_LOAD_FACTOR;
    private HashFunction hashFunc = HashFunction.v_1_6;
    private int concurrencyLevel = Constants.DEFAULT_CONCURRENCY_LEVEL;
    private EntryStrength entryStrength = EntryStrength.STRONG;
    private IMapObserver mapObserver = null;
    private String jmxName;

    public EntryStrength getEntryStrength() {
        return entryStrength;
    }

    public String getEntryStrengthName() {
        return entryStrength.name();
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    public String getHashFuncName() {
        return hashFunc.getName();
    }

    public HashFunction getHashFunc() {
        return hashFunc;
    }

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public boolean getHasMapNotifier() {
        return mapObserver !=null;
    }

    public IMapObserver getMapNotifier() {
        return mapObserver;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public void setLoadFactor(float loadFactor) {
        this.loadFactor = loadFactor;
    }

    public void setHashFunc(HashFunction hashFunc) {
        this.hashFunc = hashFunc;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public void setEntryStrength(EntryStrength entryStrength) {
        this.entryStrength = entryStrength;
    }

    public void setMapNotifier(IMapObserver mapObserver) {
        this.mapObserver = mapObserver;
    }

    public String getJmxName() {
        return jmxName;
    }

    public void setJmxName(String jmxName) {
        this.jmxName = jmxName;
    }
}
