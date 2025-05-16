package com.habbashx.manager;

import com.habbashx.property.PropertiesStore;
import com.habbashx.property.PropertyElement;
import com.habbashx.property.PropertyValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Properties;

/**
 * This class manages loading, storing, and retrieving properties from a file using
 * an associated {@link PropertiesStore}. It integrates with Java's {@link Properties}
 * system to persist and restore property configurations.
 */
public class PropertiesManager {

    /**
     * An immutable and final instance of `PropertiesStore` that acts as a container
     * for storing and managing property key-value mappings within the `PropertiesManager` class.
     *
     * This object serves as the primary source of property elements, enabling read and write operations
     * through its API. The `PropertiesStore` encapsulates a mapping of keys to their associated `PropertyElement`
     * instances, which contain property metadata and value details.
     *
     * The `propertiesStore` is the backbone of property management, providing functionalities such as:
     * - Storing new properties via `addProperty`.
     * - Retrieving property elements based on keys.
     * - Querying raw or converted property values.
     * - Updating raw or converted values of existing properties.
     * - Removing properties by their keys.
     *
     * The contents of `propertiesStore` are updated dynamically based on operations performed
     * in classes like `PropertiesManager`.
     */
    private final PropertiesStore propertiesStore;
    private final File file;
    private final Properties properties = new Properties();

    public PropertiesManager(PropertiesStore propertiesStore , File file) {
        this.propertiesStore = propertiesStore;
        this.file = file;
        loadProperties();
    }

    /**
     * Persists the current property values from the {@code PropertiesStore} into the internal
     * {@code Properties} object and subsequently saves them to the corresponding file.
     *
     * This method iterates over all {@code PropertyElement} instances in the {@code PropertiesStore},
     * retrieves their raw values through their respective keys, and sets these values in
     * the internal {@code Properties} object. The persisted properties are then written
     * to disk using {@link #storeProperties()}.
     *
     * Steps performed:
     * 1. Iterates over all {@code PropertyElement} entries in the {@code PropertiesStore}.
     * 2. Updates the internal {@code Properties} object with the key-value pairs, where the
     *    key is the property key and the value is the raw value of the property.
     * 3. Calls {@link #storeProperties()} to write the updated properties to disk.
     *
     * Throws a runtime exception if an error occurs during the disk storage process.
     */
    public void store() {
        for (PropertyElement element : propertiesStore.getPropertyElements().values()) {
            properties.setProperty(element.getKey(),element.getPropertyValue().getRawValue());
        }
        storeProperties();
    }

    /**
     * Saves the current properties from the internal {@code Properties} object
     * to the associated file without adding any comments.
     *
     * This method delegates the actual storage process to the overloaded
     * {@code storeProperties} method with an empty string for comments.
     * The internal properties are written to the disk, and any
     * IOException during this process will result in a runtime exception.
     *
     * Throws:
     * - RuntimeException if an error occurs during the file storage operation.
     */
    public void storeProperties() {
        storeProperties("");
    }

    /**
     * Saves the current properties to the specified file along with optional comments.
     *
     * This method writes the properties contained in the internal {@code Properties} object
     * to the file provided via the {@code file} attribute. An optional comment can be
     * included at the top of the file to describe the properties being stored.
     *
     * @param comments a string containing comments to be added to the top of the properties file;
     *                 can be an empty string if no comments are needed
     * @throws RuntimeException if an {@code IOException} occurs during the saving process
     */
    public void storeProperties(String comments) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream,comments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads properties from a file into the application's property store.
     * This method reads from the file specified by the `file` instance variable,
     * parses the properties, and updates the `propertiesStore` with the loaded key-value pairs.
     *
     * The method performs the following actions:
     * - Clears the existing property elements in the `propertiesStore`.
     * - Reads key-value pairs from the properties file and converts them into `PropertyValue` and `PropertyElement` objects.
     * - Populates the `propertiesStore` with these `PropertyElement` objects.
     *
     * If an I/O error occurs while reading the properties file, a `RuntimeException` is thrown.
     *
     * Throws:
     * - RuntimeException if an IOException is encountered while reading the properties file.
     */
    public void loadProperties() {
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);

            propertiesStore.getPropertyElements().clear();

            for (String key : properties.stringPropertyNames()) {
                String rawValue = properties.getProperty(key);
                PropertyValue value = new PropertyValue(rawValue);
                PropertyElement propertyElement = new PropertyElement(key,value);
                propertiesStore.addProperty(key,value);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the associated {@code PropertiesStore} instance.
     *
     * @return the {@code PropertiesStore} object that stores the application's property elements.
     */
    public PropertiesStore getPropertiesStore() {
        return propertiesStore;
    }
}
