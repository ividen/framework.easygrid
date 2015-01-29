package ru.kwanza.easygrid.map.impl;

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

/**
 * @author Alexander Guzanov
 */
class StrongEntry<K, V> implements EntryReference<K, V>{
    private K key;
    private volatile V value;
    private final int hash;

    StrongEntry(K key, V value, int hash) {
        this.key = key;
        this.value = value;
        this.hash = hash;
    }

    public boolean isKeyEqual(Object key, int hash) {
        return this.hash == hash && key.equals(this.key);
    }

    public boolean isValueEqual(Object value) {
        if (value == null && this.value != null) {
            return false;
        } else if (value == null && this.value == null) {
            return true;
        }
        return value.equals(this.value);
    }

    public V value() {
        return value;
    }

    public K key() {
        return key;
    }

    public int hash() {
        return hash;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isValid() {
        return true;
    }
}
