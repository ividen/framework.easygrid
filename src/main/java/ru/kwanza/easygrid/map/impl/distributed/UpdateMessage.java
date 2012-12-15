package ru.kwanza.easygrid.map.impl.distributed;

import ru.kwanza.easygrid.map.impl.HashEntry;
import ru.kwanza.easygrid.map.impl.HashEntry;

import java.io.Serializable;

class UpdateMessage implements Serializable {
    private HashEntry<Serializable, Serializable> entry;

    public UpdateMessage(HashEntry<Serializable, Serializable> entry) {
        this.entry = entry;
    }

    public HashEntry<Serializable, Serializable> getEntry() {
        return entry;
    }

}
