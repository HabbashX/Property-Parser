package com.habbashx.property;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a property value with both its raw and converted forms.
 * This class is designed to handle the underlying representations of a property
 * in its original string format (raw value) and its potentially processed representation (converted value).
 * It also provides functionality for equality comparison and hashing based on the property values.
 */
public class PropertyValue implements Serializable {

    @Serial
    private static final long serialVersionUID = -4221062570424312940L;

    /**
     * Represents the raw value of a property as a string. This value is typically
     * the unprocessed or original representation of the property's data.
     */
    private String rawValue;
    /**
     * Represents the processed or transformed value of a property.
     * This variable holds the converted form of the property's data,
     * which may differ from its original raw representation.
     */
    private Object convertedValue;

    public PropertyValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public void setConvertedValue(Object convertedValue) {
        this.convertedValue = convertedValue;
    }

    public String getRawValue() {
        return rawValue;
    }

    public Object getConvertedValue() {
        return convertedValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        PropertyValue that = (PropertyValue) object;
        return Objects.equals(rawValue, that.rawValue) && Objects.equals(convertedValue, that.convertedValue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(rawValue);
        result = 31 * result + Objects.hashCode(convertedValue);
        return result;
    }

    @Override
    public String toString() {
        return "PropertyValue{" +
                "rawValue='" + rawValue + '\'' +
                ", convertedValue=" + convertedValue +
                '}';
    }
}
