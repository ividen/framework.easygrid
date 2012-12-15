package ru.kwanza.easygrid.map.impl;

import java.io.IOException;

/**
 * @author Alexander Guzanov
 */
public interface AbstractConcurrentHashMapMBean {

    public int estimatedCount();

    public void printContent();

    public void printContentToFile(String filename) throws IOException;

    public void saveToFile(String fileName) throws IOException;

    public void clear();

    public int getSegmentCount();
}
