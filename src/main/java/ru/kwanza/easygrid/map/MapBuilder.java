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

import ru.kwanza.easygrid.map.impl.distributed.DistributedMap;
import ru.kwanza.easygrid.map.impl.distributed.Type;
import ru.kwanza.easygrid.map.impl.tol.MapConfigWithTOL;
import ru.kwanza.easygrid.map.impl.tol.TOLConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTL;
import ru.kwanza.easygrid.map.impl.ttl.TTLConcurrentHashMap;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.conf.XmlConfigurator;
import ru.kwanza.easygrid.map.impl.*;
import ru.kwanza.easygrid.map.impl.distributed.DistributedMap;
import ru.kwanza.easygrid.map.impl.distributed.Type;
import ru.kwanza.easygrid.map.impl.tol.MapConfigWithTOL;
import ru.kwanza.easygrid.map.impl.tol.TOLConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTL;
import ru.kwanza.easygrid.map.impl.ttl.TTLConcurrentHashMap;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Guzanov
 */
public class MapBuilder {

    protected MapConfig config;

    MapBuilder(MapConfig config) {
        this.config = config;
    }

    public static MapBuilder simple() {
        return new MapBuilder(new MapConfig());
    }

    public <K, V> IConcurrentMap<K, V> newMap() {
        return new SimpleConcurrentHashMap<K, V>(config);
    }

    public DistributedMapBuilder distributed(String clusterName) {
        return new DistributedMapBuilder(clusterName, this);
    }

    public static class DistributedMapBuilder extends MapBuilder {
        private String clusterName;
        private ProtocolStackConfigurator configurator;
        private Type type = Type.ASYNC;
        private MapBuilder baseBuilder;
        private long updateStateTimeout = 3 * 60000;
        private long castMessageTimeout = 20000;
        private String jmxName = null;

        DistributedMapBuilder(String clusterName, MapBuilder baseBuilder) {
            super(baseBuilder.config);
            this.clusterName = clusterName;
            this.baseBuilder = baseBuilder;
        }

        public DistributedMapBuilder configurator(ProtocolStackConfigurator configurator) {
            this.configurator = configurator;
            return this;
        }

        public DistributedMapBuilder replicationType(Type type) {
            this.type = type;
            return this;
        }

        public DistributedMapBuilder configurator(String fileName) {
            try {
                File f = new File(fileName);
                if (!f.exists()) {
                    throw new RuntimeException("Resource " + fileName + " not found!");
                }

                InputStream is = new FileInputStream(fileName);
                this.configurator = XmlConfigurator.getInstance(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public <K, V> IConcurrentMap<K, V> newMap() {
            try {
                return new DistributedMap(clusterName, configurator,
                        (AbstractConcurrentHashMap<K, V, ?>) baseBuilder.<Serializable, Serializable>newMap(),
                        type, updateStateTimeout, castMessageTimeout, jmxName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public DistributedMapBuilder mapNotifier(IMapObserver mapNotifier) {
            baseBuilder.mapNotifier(mapNotifier);
            return this;
        }

        @Override
        public DistributedMapBuilder soft() {
            baseBuilder.soft();
            return this;
        }

        @Override
        public DistributedMapBuilder weak() {
            super.weak();
            return this;
        }

        @Override
        public DistributedMapBuilder initialCapacity(int initialCapacity) {
            baseBuilder.initialCapacity(initialCapacity);
            return this;
        }

        @Override
        public DistributedMapBuilder loadFactor(float loadFactor) {
            baseBuilder.loadFactor(loadFactor);
            return this;
        }

        @Override
        public DistributedMapBuilder concurrencyLevel(int concurrencyLevel) {
            baseBuilder.concurrencyLevel(concurrencyLevel);
            return this;
        }

        @Override
        public DistributedMapBuilder hash(HashFunction hashFunction) {
            baseBuilder.hash(hashFunction);
            return this;
        }

        @Override
        public DistributedMapBuilder distributed(String clusterName) {
            throw new UnsupportedOperationException("Map is already distributed!");
        }

        public DistributedMapBuilder updateStateTimeout(long updateStateTimeout) {
            this.updateStateTimeout = updateStateTimeout;
            return this;
        }

        public DistributedMapBuilder castMessageTimeout(long castMessageTimeout) {
            this.castMessageTimeout = castMessageTimeout;
            return this;
        }

        @Override
        public DistributedMapBuilder jmx(String domainName, String name) {
            this.jmxName = getMBeanName(domainName, name);
            baseBuilder.jmx(jmxName);
            return this;
        }

        @Override
        public DistributedMapBuilder jmx(String prefix) {
            this.jmxName = prefix + ", replicatedMap=DistributedMap";
            baseBuilder.jmx(prefix);
            return this;
        }
    }

    public static MapBuilder ttl(long ttlTimeout, TimeUnit ttlTimeUnit) {
        return new MapBuilder(new MapConfigWithTTL(ttlTimeout, ttlTimeUnit)) {
            public <K, V> IConcurrentMap<K, V> newMap() {
                return new TTLConcurrentHashMap<K, V>((MapConfigWithTTL) config);
            }
        };
    }

    public static MapBuilder ttl(long timeoutMilliseconds) {
        return ttl(timeoutMilliseconds, TimeUnit.MILLISECONDS);
    }

    public static MapBuilder tol(long ttlTimeout, TimeUnit ttlTimeUnit, long tolTimeout, TimeUnit tolTimeUnit) {
        return new MapBuilder(new MapConfigWithTOL(ttlTimeout, ttlTimeUnit, tolTimeout, tolTimeUnit)) {
            public <K, V> IConcurrentMap<K, V> newMap() {
                return new TOLConcurrentHashMap<K, V>((MapConfigWithTTL) config);
            }
        };
    }

    public static MapBuilder tol(long ttlTimeoutMilliseconds, long tolTimeoutMilliseconds) {
        return tol(ttlTimeoutMilliseconds, TimeUnit.MILLISECONDS, tolTimeoutMilliseconds, TimeUnit.MILLISECONDS);
    }

    public MapBuilder mapNotifier(IMapObserver mapNotifier) {
        config.setMapNotifier(mapNotifier);
        return this;
    }

    public MapBuilder soft() {
        config.setEntryStrength(EntryStrength.SOFT);
        return this;
    }

    public MapBuilder weak() {
        config.setEntryStrength(EntryStrength.WEAK);
        return this;
    }

    public MapBuilder jmx(String domainName, String name) {
        config.setJmxName(getMBeanName(domainName, name));
        return this;
    }

    public MapBuilder jmx(String prefix) {
        config.setJmxName(prefix + ", map=IConcurrentMap");
        return this;
    }

    public MapBuilder initialCapacity(int initialCapacity) {
        config.setInitialCapacity(initialCapacity);
        return this;
    }

    public MapBuilder loadFactor(float loadFactor) {
        config.setLoadFactor(loadFactor);
        return this;
    }

    public MapBuilder concurrencyLevel(int concurrencyLevel) {
        config.setConcurrencyLevel(concurrencyLevel);
        return this;
    }

    public MapBuilder hash(HashFunction hashFunction) {
        config.setHashFunc(hashFunction);
        return this;
    }

    private static String getMBeanName(String domainName, String name) {
        return domainName + ": package=com.kwanza, type=easygrid, name=" + name;
    }

}
