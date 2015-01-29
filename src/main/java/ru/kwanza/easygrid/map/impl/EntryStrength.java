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
public enum EntryStrength {
    STRONG() {
        public <K, V> EntryReference<K, V> reference(K key, V value, int hash, BaseSegment<K, V, ?> segment) {
            return new StrongEntry<K, V>(key, value, hash);
        }
    },

    SOFT() {
        public <K, V> EntryReference<K, V> reference(K key, V value, int hash, BaseSegment<K, V, ?> segment) {
            return new SoftEntry<K, V>(key, value, hash, segment);
        }
    },
    WEAK() {
        public <K, V> EntryReference<K, V> reference(K key, V value, int hash, BaseSegment<K, V, ?> segment) {
            return new WeakEntry<K, V>(key, value, hash, segment);
        }
    };

    public abstract <K, V> EntryReference<K, V> reference(K key, V value, int hash, BaseSegment<K, V, ?> segment);

}
