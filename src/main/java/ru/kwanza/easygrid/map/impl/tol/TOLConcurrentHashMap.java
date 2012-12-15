package ru.kwanza.easygrid.map.impl.tol;

import ru.kwanza.easygrid.map.impl.IMapConfig;
import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTL;
import ru.kwanza.easygrid.map.impl.ttl.TTLConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.ttl.TTLSegment;
import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTL;
import ru.kwanza.easygrid.map.impl.ttl.TTLConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.ttl.TTLSegment;

/**
 * Tenaciouse-Of-Life Concurrent Hash Map
 *
 * @author Alexander Guzanov
 */
public class TOLConcurrentHashMap<K, V> extends TTLConcurrentHashMap<K, V> {
    public TOLConcurrentHashMap(MapConfigWithTTL config) {
        super(config);
    }

    protected TTLSegment<K, V> allocateSegment(IMapConfig config, int cap) {
        MapConfigWithTOL configWithTOL = (MapConfigWithTOL) config;
        return new TOLSegment<K, V>(config.getEntryStrength(), cap, config.getLoadFactor(),
                configWithTOL.getTTLTimeout(), configWithTOL.getTTLTimeUnit(), configWithTOL.getTOLTimeout(),
                configWithTOL.getTOLTimeUnit());
    }
}