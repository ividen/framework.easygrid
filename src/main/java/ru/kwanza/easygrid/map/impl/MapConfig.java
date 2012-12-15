package ru.kwanza.easygrid.map.impl;

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
