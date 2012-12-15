package ru.kwanza.easygrid.map.impl;

/**
 * @author Alexander Guzanov
 */
public interface Constants {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    public static final int MAXIMUM_CAPACITY = 1 << 30;
    public static final int MAX_SEGMENTS = 1 << 16;
    public static final int RETRIES_BEFORE_LOCK = 2;
}
