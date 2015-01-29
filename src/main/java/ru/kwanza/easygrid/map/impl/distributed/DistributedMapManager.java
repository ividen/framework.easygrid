package ru.kwanza.easygrid.map.impl.distributed;

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

import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.IConcurrentMap;

import java.util.HashSet;
import java.util.Set;

public class DistributedMapManager {
    Set<DistributedMap> repository = new HashSet<DistributedMap>();
    static DistributedMapManager instance = new DistributedMapManager();

    public static DistributedMapManager getInstance() {
        return instance;
    }

    void addMap(DistributedMap map) {
        repository.add(map);
    }

    public void close(IConcurrentMap map) {
        if (repository.remove(map)) {
            ((DistributedMap) map).close();
        }
    }

}
