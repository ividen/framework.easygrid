package ru.kwanza.easygrid.map.impl;

import ru.kwanza.easygrid.map.IMapObserver;

import java.io.Serializable;

/**
 * @author Alexander Guzanov
 */
public interface IMapConfig extends Serializable {

    public EntryStrength getEntryStrength();

    public int getInitialCapacity();

    public float getLoadFactor();

    public HashFunction getHashFunc();

    public int getConcurrencyLevel();

    public String getJmxName();

    IMapObserver getMapNotifier();
}
