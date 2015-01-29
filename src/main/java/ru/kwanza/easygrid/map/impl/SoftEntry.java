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

import java.lang.ref.SoftReference;

/**
 * @author Alexander Guzanov
 */
class SoftEntry<K, V> extends SoftReference<StrongEntry<K, V>>
        implements EntryReference<K, V> {
    BaseSegment<K, V, ?> segment;
    private final int hash;

    public SoftEntry(K key, V value, int hash, BaseSegment<K, V, ?> segment) {
        super(new StrongEntry<K, V>(key, value, hash), References.REFERENCE_QUEUE);
        this.segment = segment;
        this.hash = hash;
    }

    public boolean isKeyEqual(Object key, int hash) {
        StrongEntry<K, V> reference = get();
        return reference != null && reference.isKeyEqual(key, hash);
    }

    public boolean isValueEqual(Object value) {
        StrongEntry<K, V> reference = get();
        return reference != null && reference.isValueEqual(value);
    }

    public V value() {
        StrongEntry<K, V> reference = get();
        return reference == null ? null : reference.value();
    }

    public K key() {
        StrongEntry<K, V> reference = get();
        return reference == null ? null : reference.key();
    }

    public int hash() {
        return hash;
    }

    public void setValue(V value) {
        StrongEntry<K, V> reference = get();
        if (reference != null) {
            reference.setValue(value);
        }
    }

    public boolean isValid() {
        return get() != null;
    }
}
