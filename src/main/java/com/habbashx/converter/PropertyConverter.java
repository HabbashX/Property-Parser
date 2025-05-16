package com.habbashx.converter;

/**
 * Defines a contract for converting a string representation of a value into
 * a specific type.
 *
 * This interface is implemented by classes that handle the conversion of raw string
 * input into a desired object type. The conversion logic is encapsulated in the
 * {@code convert} method, which takes a string value as input and transforms it into
 * an object of the specified type {@code T}.
 *
 * @param <T> The type of the object to which the value will be converted.
 */
public interface PropertyConverter<T> {
    /**
     * Converts the given string value into an object of type {@code T}.
     *
     * This method takes a {@code String} input and performs a type conversion
     * to produce an object of the specified type {@code T}. The actual
     * conversion logic is implemented by the class that provides the
     * specific implementation of this method.
     *
     * @param value The string value to be converted. Can be null or in a specific
     *              format required by the conversion implementation.
     * @return The converted object of type {@code T}.
     */
    T convert(String value);
}
