package ru.kwanza.easygrid.map.impl.distributed;

/**
 * @author Alexander Guzanov
 */
public interface DistributedMapMBean {

    public String getClusterName();

    public String getReplicationTypeName();

    public long getUpdateStateTimeout();

    public long getCastMessageTimeout();
}
