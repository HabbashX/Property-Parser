package com.habbashx.converter;

/**
 * Generic contract for converting raw configuration values into typed objects.
 *
 * <p>
 * This interface is used by the property injection system to transform
 * string-based property values into strongly-typed Java objects.
 * </p>
 *
 * <p>
 * Implementations can define custom parsing logic for specific types
 * such as enums, complex objects, or formatted strings.
 * </p>
 *
 * @param <T> the target type produced by this converter
 */
public interface PropertyConverter<T> {

    /**
     * Converts a raw string value into a strongly-typed object.
     *
     * @param type the target class type expected as output (useful for generic handling)
     * @param rawValue the raw string value from the property source
     * @return the converted value of type {@code T}
     * @throws RuntimeException if conversion fails or the value is invalid
     */
    T convert(Class<?> type, String rawValue);
}