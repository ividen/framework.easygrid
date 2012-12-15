package ru.kwanza.easygrid.map.impl;

/**
 * @author Alexander Guzanov
 */
public interface MapConfigMBean {

    public String getEntryStrengthName();

    public int getInitialCapacity();

    public float getLoadFactor();

    public String getHashFuncName();

    public int getConcurrencyLevel();

    public boolean getHasMapNotifier();

}
