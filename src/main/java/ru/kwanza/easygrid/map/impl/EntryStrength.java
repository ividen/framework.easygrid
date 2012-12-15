package ru.kwanza.easygrid.map.impl;

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
