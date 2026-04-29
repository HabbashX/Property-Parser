package com.habbashx.injector.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation of {@link PropertySource} that loads properties from a file.
 * <p>
 * This class reads a standard Java {@link Properties} file (.properties) and provides
 * access to its values through the {@link PropertySource} interface.
 * The properties are loaded once during construction and stored in memory.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     PropertySource source = new FilePropertySource(new File("config.properties"));
 *     String value = source.get("app.name");
 * </pre>
 *
 * <p>Thread-safety: This implementation is effectively read-only after construction,
 * assuming the underlying {@link Properties} object is not modified externally.</p>
 */
public class FilePropertySource implements PropertySource {

    /**
     * Internal storage for loaded key-value pairs from the properties file.
     */
    private final Properties properties = new Properties();

    /**
     * Loads properties from the specified file into memory.
     *
     * <p>This constructor reads the provided file using a {@link FileInputStream}
     * and loads all key-value pairs into an internal {@link Properties} instance.</p>
     *
     * @param file the properties file to load; must be a valid readable file
     * @throws RuntimeException if the file cannot be read or an I/O error occurs
     */
    public FilePropertySource(File file) {

        try (final InputStream stream = new FileInputStream(file)){

            properties.load(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a property value by its key.
     *
     * @param key the property key
     * @return the corresponding value, or {@code null} if the key does not exist
     */
    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Returns all loaded properties.
     *
     * <p>Note: Modifying the returned object will affect the internal state.</p>
     *
     * @return all properties loaded from the file
     */
    @Override
    public Properties getAll() {
        return properties;
    }
}

