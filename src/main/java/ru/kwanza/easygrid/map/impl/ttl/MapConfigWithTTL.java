package ru.kwanza.easygrid.map.impl.ttl;

import ru.kwanza.easygrid.map.impl.MapConfig;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class MapConfigWithTTL extends MapConfig implements MapConfigWithTTLMBean {
    private TimeUnit ttlTimeUnit = TimeUnit.SECONDS;
    private long ttlTimeout = 1;

    public MapConfigWithTTL(long ttlTimeout, TimeUnit ttlTimeUnit) {
        this.ttlTimeUnit = ttlTimeUnit;
        this.ttlTimeout = ttlTimeout;
    }

    public long getTTLTimeout() {
        return ttlTimeout;
    }

    public String getTTLTimeUnitName() {
        return ttlTimeUnit.name();
    }

    public TimeUnit getTTLTimeUnit() {
        return ttlTimeUnit;
    }
}
