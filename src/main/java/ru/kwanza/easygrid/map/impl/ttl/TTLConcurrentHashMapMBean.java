package ru.kwanza.easygrid.map.impl.ttl;

import ru.kwanza.easygrid.map.impl.AbstractConcurrentHashMapMBean;

/**
 * @author Alexander Guzanov
 */
public interface TTLConcurrentHashMapMBean extends AbstractConcurrentHashMapMBean {

    public void shrink();
}
