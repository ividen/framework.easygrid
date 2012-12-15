package ru.kwanza.easygrid.map.impl.ttl;

import ru.kwanza.easygrid.map.impl.MapConfigMBean;
import ru.kwanza.easygrid.map.impl.MapConfigMBean;

/**
 * @author Alexander Guzanov
 */
public interface MapConfigWithTTLMBean extends MapConfigMBean {

    public long getTTLTimeout();

    public String getTTLTimeUnitName();
}
