package com.habbashx.property;


import com.habbashx.validation.PropertyValidator;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a property element characterized by a unique key and a corresponding value.
 * A property element can have multiple validators to enforce specific validation rules
 * on its associated value.
 *
 * This class ensures immutability for its key and property value while supporting the
 * addition of property validators. It provides mechanisms to check the validity of its
 * property value using the registered validators.
 *
 * Implements Serializable for potential persistence or transport purposes.
 */
public class PropertyElement implements Serializable {

    @Serial
    private static final long serialVersionUID = -7386949509464954824L;

    /**
     * A unique identifier for the property element.
     * This key is used to distinguish the property element and is immutable.
     */
    private final String key;
    /**
     * Represents the value of a property within a {@link PropertyElement}.
     *
     * This variable is immutable and encapsulates the property's raw and potentially converted values.
     * It is used to facilitate validation and manipulation of property data through various frameworks,
     * such as the {@link PropertyValidator}.
     *
     * The {@code propertyValue} is essential for determining the correctness of a property using its
     * associated validators. It supports equality checks, hash-based containers, and string
     * representations via the corresponding methods in the {@link PropertyValue} class.
     */
    private final PropertyValue propertyValue;

    /**
     * A list of property validators used to validate the properties of a model or entity.
     * Each validator in the list is a distinct implementation of the PropertyValidator interface,
     * allowing for customizable and modular validation logic.
     *
     * This list supports adding, removing, and iterating over validators, enabling flexible
     * and dynamic validation configurations. The validators are typically applied in sequence
     * to validate the specified properties.
     */
    private final List<PropertyValidator> validators = new ArrayList<>();

    /**
     * Constructs a new {@code PropertyElement} instance.
     *
     * @param key the unique identifier for this property
     * @param propertyValue an instance of {@code PropertyValue} containing the raw and converted values of the property
     */
    public PropertyElement(String key, PropertyValue propertyValue) {
        this.key = key;
        this.propertyValue = propertyValue;

    }

    /**
     * Adds a {@code PropertyValidator} to the list of validators.
     *
     * @param propertyValidator the validator to be added
     */
    public void addValidator(PropertyValidator propertyValidator) {
        validators.add(propertyValidator);
    }

    /**
     * Validates the property value against all registered validators.
     * Each validator is applied sequentially to the raw value of the property,
     * and if any validator fails, the method returns false.
     *
     * @return {@code true} if all validators consider the property value valid;
     *         {@code false} otherwise.
     */
    public boolean isValid() {
        for (PropertyValidator validator : validators) {
            if (!validator.isValid(propertyValue.getRawValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the raw value of the property using the specified {@code PropertyValidator}.
     *
     * @param propertyValidator the validator used to check the validity of the property's raw value
     * @return {@code true} if the raw value is valid according to the provided validator, {@code false} otherwise
     */
    public boolean isValid(@NotNull PropertyValidator propertyValidator) {
        return propertyValidator.isValid(propertyValue.getRawValue());
    }

    /**
     * Retrieves the unique identifier associated with this property element.
     *
     * @return the key representing the unique identifier of the property
     */
    public String getKey() {
        return key;
    }

    /**
     * Retrieves the property value associated with this {@code PropertyElement}.
     *
     * @return the {@code PropertyValue} instance containing the raw and converted values of the property
     */
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

    /**
     * Returns a string representation of the {@code PropertyElement} object.
     * The returned string includes the values of the {@code key} and {@code propertyValue} fields.
     *
     * @return a string representation of the {@code PropertyElement} object
     */
    @Override
    public String toString() {
        return "PropertyElement{" +
                "key='" + key + '\'' +
                ", propertyValue=" + propertyValue +
                '}';
    }
}
