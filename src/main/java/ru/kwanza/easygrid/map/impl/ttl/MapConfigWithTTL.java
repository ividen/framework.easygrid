package ru.kwanza.easygrid.map.impl.ttl;

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
