package com.habbashx.property;


import com.habbashx.validation.PropertyValidator;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PropertyElement implements Serializable {

    @Serial
    private static final long serialVersionUID = -7386949509464954824L;

    private final String key;
    private final PropertyValue propertyValue;

    private final List<PropertyValidator> validators = new ArrayList<>();

    public PropertyElement(String key, PropertyValue propertyValue) {
        this.key = key;
        this.propertyValue = propertyValue;

    }

    public void addValidator(PropertyValidator propertyValidator) {
        validators.add(propertyValidator);
    }

    public boolean isValid() {
        for (PropertyValidator validator : validators) {
            if (!validator.isValid(propertyValue.getRawValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid(@NotNull PropertyValidator propertyValidator) {
        return propertyValidator.isValid(propertyValue.getRawValue());
    }

    public String getKey() {
        return key;
    }

    public PropertyValue getPropertyValue() {
        return propertyValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        PropertyElement that = (PropertyElement) object;
        return key.equals(that.key) && propertyValue.equals(that.propertyValue);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + propertyValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PropertyElement{" +
                "key='" + key + '\'' +
                ", propertyValue=" + propertyValue +
                '}';
    }
}
