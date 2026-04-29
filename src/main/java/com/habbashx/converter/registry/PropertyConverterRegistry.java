package com.habbashx.converter.registry;

import com.habbashx.converter.PropertyConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry responsible for managing and caching {@link PropertyConverter} instances.
 *
 * <p>
 * This class acts as a central container for all converters used in the property
 * injection system. It allows converters to be registered manually or instantiated
 * lazily on first use.
 * </p>
 *
 * <p>
 * The registry ensures that each converter type is reused (cached) to avoid
 * unnecessary object creation during repeated conversions.
 * </p>
 *
 * <p>
 * Thread-safety: Uses {@link ConcurrentHashMap} to safely support concurrent access.
 * </p>
 */
public class PropertyConverterRegistry {

    /**
     * Internal registry mapping converter classes to their instances.
     */
    private final Map<Class<? extends PropertyConverter<?>>, PropertyConverter<?>> registry =
            new ConcurrentHashMap<>();

    /**
     * Registers a custom converter instance for a specific converter type.
     *
     * @param type the converter class type
     * @param converter the converter instance to register
     * @param <T> the target conversion type
     */
    public <T> void register(Class<? extends PropertyConverter<T>> type,
                             PropertyConverter<T> converter) {
        registry.put(type, converter);
    }

    /**
     * Converts a raw string value into a target type using the specified converter.
     *
     * <p>
     * If the converter is not already registered, it will be instantiated
     * using its no-argument constructor and cached for future use.
     * </p>
     *
     * @param converterClass the converter class to use
     * @param targetType the expected output type (used by converter logic)
     * @param rawValue the raw string value to convert
     * @return the converted object
     * @throws RuntimeException if the converter cannot be instantiated
     */
    @SuppressWarnings("unchecked")
    public Object convert(Class<? extends PropertyConverter<?>> converterClass,
                          Class<?> targetType,
                          String rawValue) {

        PropertyConverter<?> converter = registry.get(converterClass);

        if (converter == null) {
            try {
                converter = converterClass.getDeclaredConstructor().newInstance();
                registry.put(converterClass, converter);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create converter: " + converterClass.getName(), e);
            }
        }

        return ((PropertyConverter<Object>) converter).convert(targetType, rawValue);

    }
}
