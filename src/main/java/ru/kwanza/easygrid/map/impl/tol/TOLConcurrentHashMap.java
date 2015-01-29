package ru.kwanza.easygrid.map.impl.tol;

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