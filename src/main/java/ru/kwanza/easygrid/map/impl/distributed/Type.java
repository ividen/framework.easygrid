package ru.kwanza.easygrid.map.impl.distributed;

import org.jgroups.blocks.GroupRequest;

/**
 * @author Alexander Guzanov
 */
public enum Type {
    SYNC(GroupRequest.GET_ALL),
    ASYNC(GroupRequest.GET_NONE);

    int code;

    Type(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
