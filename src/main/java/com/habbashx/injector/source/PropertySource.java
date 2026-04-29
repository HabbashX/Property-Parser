package com.habbashx.injector.source;

import java.util.Properties;

/**
 * Abstraction for a source of configuration properties.
 * <p>
 * This interface defines a contract for retrieving key-value configuration data
 * from any underlying storage mechanism such as files, databases, remote services,
 * or in-memory maps.
 * </p>
 *
 * <p>
 * Implementations are responsible for how properties are loaded and stored,
 * while consumers of this interface only rely on the retrieval methods.
 * </p>
 *
 * <p>Common implementations may include:</p>
 * <ul>
 *     <li>File-based property sources (e.g., .properties files)</li>
 *     <li>Environment variable-based sources</li>
 *     <li>Remote configuration services</li>
 * </ul>
 */
public interface PropertySource {

    /**
     * Retrieves a property value associated with the given key.
     *
     * @param key the property name to look up
     * @return the corresponding value, or {@code null} if the key is not found
     */
    String get(String key);

    /**
     * Returns all properties available in this source.
     *
     * <p>
     * The returned {@link Properties} object may be mutable depending on the implementation.
     * Modifying it may affect the underlying source if not properly encapsulated.
     * </p>
     *
     * @return all loaded properties as a {@link Properties} instance
     */
    Properties getAll();

}
