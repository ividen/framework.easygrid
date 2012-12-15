package ru.kwanza.easygrid.map.impl.tol;

import ru.kwanza.easygrid.map.impl.ttl.MapConfigWithTTLMBean;

/**
 * @author Alexander Guzanov
 */
public interface MapConfigWithTOLMBean extends MapConfigWithTTLMBean {
    public long getTOLTimeout();

    public String getTOLTimeUnitName();
}
