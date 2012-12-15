package ru.kwanza.easygrid.map.impl;

/**
 * @author Alexander Guzanov
 */
public class SimpleConcurrentHashMap<K  , V  >
        extends AbstractConcurrentHashMap<K, V, HashEntry<K, V>> {
    public SimpleConcurrentHashMap(MapConfig config) {
        super(config);
    }

    protected BaseSegment<K, V, HashEntry<K, V>> allocateSegment(IMapConfig config, int cap) {
        return new BaseSegment<K, V, HashEntry<K, V>>(config.getEntryStrength(), cap, config.getLoadFactor());
    }

}
