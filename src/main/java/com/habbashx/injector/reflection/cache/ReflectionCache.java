package com.habbashx.injector.reflection.cache;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache utility for reflection-based field lookup.
 * <p>
 * This class is designed to improve performance of reflection-heavy operations
 * by caching declared fields of classes. Instead of repeatedly calling
 * {@code Class.getDeclaredFields()}, results are stored in memory after the first lookup.
 * </p>
 *
 * <p>
 * It also automatically sets fields as accessible, removing the need for repeated
 * {@code setAccessible(true)} calls during injection or mapping processes.
 * </p>
 *
 * <p>
 * Thread-safety: This implementation uses {@link ConcurrentHashMap}, making it safe
 * for concurrent access in multi-threaded environments.
 * </p>
 */
public class ReflectionCache {

    /**
     * Internal cache mapping a class to its declared fields.
     */
    private final Map<Class<?>, List<Field>> reflectionCache = new ConcurrentHashMap<>();

    /**
     * Retrieves cached declared fields for the given class.
     *
     * <p>
     * If the class is not yet cached, its declared fields are retrieved via reflection,
     * made accessible, and stored for future reuse.
     * </p>
     *
     * @param type the class whose fields should be retrieved
     * @return a list of declared fields with accessibility already enabled
     */
    public List<Field> getFields(Class<?> type) {
        return reflectionCache.computeIfAbsent(type, clazz -> {
            List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
            fields.forEach(f -> f.setAccessible(true));
            return fields;
        });
    }
}
