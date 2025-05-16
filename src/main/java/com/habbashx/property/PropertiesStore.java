package com.habbashx.property;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that manages a collection of properties, represented as key-value pairs. Each property is
 * stored as a {@link PropertyElement}, which encapsulates a key and a {@link PropertyValue} object.
 * This class provides various utility methods for adding, retrieving, updating, and removing properties
 * and for handling their raw and converted values.
 *
 * This class is serializable to allow its state to be persisted and restored.
 */
public class PropertiesStore implements Serializable {

    @Serial
    private static final long serialVersionUID = 1554786141346401493L;

    /**
     * A map that stores property elements, where the key is a unique identifier
     * (String) and the value is a {@link PropertyElement} object. Each
     * {@link PropertyElement} represents a property with its associated raw and
     * converted values, along with optional validations.
     *
     * This map serves as the core storage structure for managing property data
     * and provides efficient retrieval and manipulation of property-related
     * information within the containing class.
     */
    private final Map<String, PropertyElement> propertyElements = new HashMap<>();

    /**
     * Adds a property to the store, associating the specified key with the given property value.
     *
     * @param key the unique identifier for the property being added
     * @param propertyValue an instance of {@code PropertyValue} containing the raw and converted values of the property to store
     */
    public void addProperty(String key, PropertyValue propertyValue) {
        PropertyElement propertyElement = new PropertyElement(key, propertyValue);
        propertyElements.put(key, propertyElement);
    }


    /**
     * Retrieves the {@code PropertyElement} associated with the given key.
     *
     * @param key the unique identifier for the property element to retrieve
     * @return the {@code PropertyElement} associated with the given key, or {@code null} if not found
     */
    public PropertyElement getPropertyElement(String key) {
        return propertyElements.get(key);
    }

    /**
     * Retrieves the raw value of the property associated with the specified key.
     * If no property exists for the given key, it returns {@code null}.
     *
     * @param key the unique identifier of the property whose raw value is to be retrieved
     * @return the raw value of the property as a {@code String}, or {@code null} if no property exists for the given key
     */
    public String getRawValue(String key) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            return propertyElement.getPropertyValue().getRawValue();
        }
        return null;
    }

    /**
     * Retrieves the converted value of the property associated with the specified key.
     * If no property exists for the given key, it returns {@code null}.
     *
     * @param key the unique identifier of the property whose converted value is to be retrieved
     * @return the converted value of the property as an {@code Object}, or {@code null} if no property exists for the given key
     */
    public Object getConvertedValue(String key) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            return propertyElement.getPropertyValue().getConvertedValue();
        }
        return null;
    }

    /**
     * Retrieves the converted value of a property associated with the specified key
     * and casts it to the requested type.
     *
     * @param <T>  the type to which the converted value should be cast
     * @param key  the unique identifier of the property whose converted value is to be retrieved
     * @param type the {@code Class} object of the type {@code T} to cast the converted value to
     * @return the converted value of the property cast to the specified type {@code T},
     *         or {@code null} if the property associated with the given key does not exist
     */
    public <T> T getConvertedValue(String key, Class<T> type) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            Object convertedValue = propertyElement.getPropertyValue().getConvertedValue();
            return type.cast(convertedValue);  // Safely cast to the requested type
        }
        return null;
    }

    /**
     * Retrieves the raw value associated with the specified key, converts it to an integer, and returns the result.
     *
     * @param key the unique identifier for the property whose integer value is to be retrieved; cannot be null
     * @return the integer representation of the raw value associated with the given key
     * @throws NumberFormatException if the raw value cannot be parsed as an integer
     * @throws NullPointerException if the key is null
     */
    public int getInt(@NotNull String key) {
        return Integer.parseInt(getRawValue(key));
    }

    /**
     * Retrieves the raw value of the property associated with the specified key,
     * parses it as a {@code long}, and returns the resulting value.
     *
     * @param key the unique identifier of the property whose value is to be retrieved and parsed as a {@code long}; must not be null
     * @return the parsed {@code long} value of the raw property value associated with the given key
     * @throws NumberFormatException if the raw property value cannot be parsed as a {@code long}
     * @throws NullPointerException if the given key is null
     */
    public long getLong(@NotNull String key) {
        return Long.parseLong(getRawValue(key));
    }

    /**
     * Retrieves a float value associated with the specified key.
     * The method fetches the raw value using the provided key, parses it, and returns it as a float.
     *
     * @param key the non-null key to retrieve the corresponding float value
     * @return the float value associated with the specified key
     */
    public float getFloat(@NotNull String key) {
        return Float.parseFloat(getRawValue(key));
    }

    /**
     * Retrieves the raw value of the property associated with the specified key,
     * parses it as a {@code double}, and returns the resulting value.
     *
     * @param key the unique identifier for the property whose double value is to be retrieved; must not be null
     * @return the parsed {@code double} value of the raw property value associated with the given key
     * @throws NumberFormatException if the raw value cannot be parsed as a double
     * @throws NullPointerException if the key is null
     */
    public double getDouble(@NotNull String key) {
        return Double.parseDouble(getRawValue(key));
    }

    /**
     * Retrieves a short value associated with the given key by parsing the raw value.
     *
     * @param key the non-null key whose associated short value is to be retrieved
     * @return the short value parsed from the raw value associated with the specified key
     * @throws NumberFormatException if the raw value cannot be parsed as a short
     * @throws NullPointerException if the key is null
     */
    public short getShort(@NotNull String key) {
        return Short.parseShort(getRawValue(key));
    }

    /**
     * Retrieves the boolean value of the property associated with the specified key.
     * The method fetches the raw value using the given key, parses it as a boolean,
     * and returns the resulting value.
     *
     * @param key the unique identifier of the property whose boolean value is to be retrieved; must not be null
     * @return {@code true} if the raw value can be successfully parsed as {@code true};
     *         {@code false} otherwise
     */
    public boolean getBoolean(@NotNull String key) {
        return Boolean.parseBoolean(getRawValue(key));
    }

    /**
     * Retrieves the first character of the given string key.
     *
     * @param key the non-null string key from which the first character is to be retrieved
     * @return the first character of the specified key
     */
    public Character getChar(@NotNull String key) {
        return key.charAt(0);
    }

    /**
     * Updates the raw value of the property associated with the specified key.
     * If no property exists for the provided key, the method does nothing.
     *
     * @param key the unique identifier of the property whose raw value is to be updated
     * @param newRawValue the new raw value to set for the property
     */
    public void updateRawValue(String key, String newRawValue) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            propertyElement.getPropertyValue().setRawValue(newRawValue);
        }
    }

    /**
     * Updates the converted value of the property associated with the specified key.
     * If no property exists for the provided key, the method does nothing.
     *
     * @param key the unique identifier of the property whose converted value is to be updated
     * @param newConvertedValue the new converted value to set for the property
     */
    public void updateConvertedValue(String key, Object newConvertedValue) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            propertyElement.getPropertyValue().setConvertedValue(newConvertedValue);
        }
    }

    /**
     * Removes the property associated with the specified key from the property store.
     *
     * @param key the unique identifier for the property to be removed; must not be null
     */
    public void removeProperty(String key) {
        propertyElements.remove(key);
    }

    /**
     * Retrieves a map of all property elements stored in the property store.
     * The map associates unique string keys with their corresponding {@code PropertyElement} objects.
     *
     * @return a map containing the property elements, where the keys are unique {@code String} identifiers
     *         and the values are {@code PropertyElement} objects
     */
    public Map<String, PropertyElement> getPropertyElements() {
        return propertyElements;
    }

    /**
     * Generates a string representation of the {@code PropertiesStore} object.
     * Includes the details of the {@code propertyElements} field.
     *
     * @return a {@code String} representation of this {@code PropertiesStore} instance
     */
    @Override
    public String toString() {
        return "PropertiesStore{" +
                "propertyElements=" + propertyElements +
                '}';
    }
}