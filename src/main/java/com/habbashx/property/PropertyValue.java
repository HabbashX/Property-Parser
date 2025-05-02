package com.habbashx.property;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class PropertyValue implements Serializable {

    @Serial
    private static final long serialVersionUID = -4221062570424312940L;

    private String rawValue;
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
