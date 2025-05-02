package com.habbashx.property;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PropertiesStore implements Serializable {

    @Serial
    private static final long serialVersionUID = 1554786141346401493L;

    private final Map<String, PropertyElement> propertyElements = new HashMap<>();

    public void addProperty(String key, PropertyValue propertyValue) {
        PropertyElement propertyElement = new PropertyElement(key, propertyValue);
        propertyElements.put(key, propertyElement);
    }


    public PropertyElement getPropertyElement(String key) {
        return propertyElements.get(key);
    }

    public String getRawValue(String key) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            return propertyElement.getPropertyValue().getRawValue();
        }
        return null;
    }

    public Object getConvertedValue(String key) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            return propertyElement.getPropertyValue().getConvertedValue();
        }
        return null;
    }

    public <T> T getConvertedValue(String key, Class<T> type) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            Object convertedValue = propertyElement.getPropertyValue().getConvertedValue();
            return type.cast(convertedValue);  // Safely cast to the requested type
        }
        return null;
    }

    public int getInt(@NotNull String key) {
        return Integer.parseInt(getRawValue(key));
    }

    public long getLong(@NotNull String key) {
        return Long.parseLong(getRawValue(key));
    }

    public float getFloat(@NotNull String key) {
        return Float.parseFloat(getRawValue(key));
    }

    public double getDouble(@NotNull String key) {
        return Double.parseDouble(getRawValue(key));
    }

    public short getShort(@NotNull String key) {
        return Short.parseShort(getRawValue(key));
    }

    public boolean getBoolean(@NotNull String key) {
        return Boolean.parseBoolean(getRawValue(key));
    }

    public Character getChar(@NotNull String key) {
        return key.charAt(0);
    }

    public void updateRawValue(String key, String newRawValue) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            propertyElement.getPropertyValue().setRawValue(newRawValue);
        }
    }

    public void updateConvertedValue(String key, Object newConvertedValue) {
        PropertyElement propertyElement = propertyElements.get(key);
        if (propertyElement != null) {
            propertyElement.getPropertyValue().setConvertedValue(newConvertedValue);
        }
    }

    public void removeProperty(String key) {
        propertyElements.remove(key);
    }

    public Map<String, PropertyElement> getPropertyElements() {
        return propertyElements;
    }

    @Override
    public String toString() {
        return "PropertiesStore{" +
                "propertyElements=" + propertyElements +
                '}';
    }
}