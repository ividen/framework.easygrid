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
