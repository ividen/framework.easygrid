package ru.kwanza.easygrid.map.impl.distributed;

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
