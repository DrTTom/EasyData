package de.tautenhahn.easydata.engine;

/**
 * Defines how to iterate over a complex type.
 */
public enum ListMode {
    /**
     * use elements of lists or arrays, keys for maps or other complex objects
     */
    DEFAULT,
    /**
     * use map keys, attribute names or numeric strings for arrays and lists
     */
    KEYS,
    /**
     * use values always
     */
    VALUES
}
