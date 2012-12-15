package ru.kwanza.easygrid.map.impl.tol;

import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTL;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class MapConfigWithTOL extends MapConfigWithTTL implements MapConfigWithTOLMBean {
    private TimeUnit tolTimeUnit = TimeUnit.SECONDS;
    private long tolTimeout = 1;

    public MapConfigWithTOL(long ttlTimeout, TimeUnit ttlTimeUnit, long tolTimeout, TimeUnit tolTimeUnit) {
        super(ttlTimeout, ttlTimeUnit);
        this.tolTimeout = tolTimeout;
        this.tolTimeUnit = tolTimeUnit;
    }

    public long getTOLTimeout() {
        return tolTimeout;
    }

    public String getTOLTimeUnitName() {
        return tolTimeUnit.name();
    }

    public TimeUnit getTOLTimeUnit() {
        return tolTimeUnit;
    }
}
