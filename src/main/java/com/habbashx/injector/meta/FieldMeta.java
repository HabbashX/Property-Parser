package com.habbashx.injector.meta;

import com.habbashx.annotation.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * FieldMeta
 * <p>
 * A cached reflection descriptor for a single field used in the PropertyInjector system.
 * <p>
 * This class stores all precomputed metadata required to perform fast field injection
 * without repeated reflection overhead.
 * <p>
 * It is immutable after construction and safe for concurrent use.
 */
public final class FieldMeta {

    /**
     * Lookup used to create MethodHandle accessors.
     */
    private static final MethodHandles.Lookup LOOKUP = lookup();

    /**
     * Original reflected field.
     */
    private final Field field;

    /**
     * Fast setter using MethodHandle (replaces Field.set).
     */
    private final MethodHandle setter;

    /**
     * Fast getter using MethodHandle (replaces Field.get).
     */
    private final MethodHandle getter;

    /**
     * True if the field is static.
     */
    private final boolean isStatic;

    /**
     * Raw field type.
     */
    private final Class<?> fieldType;

    /**
     * Generic type (used for lists / parameterized types).
     */
    private final Type genericType;

    /**
     * Injects simple property value.
     */
    private final InjectProperty injectProperty;

    /**
     * Injects nested object using prefix.
     */
    private final InjectPrefix injectPrefix;

    /**
     * Injects list values.
     */
    private final InjectList injectList;

    /**
     * Default fallback value if property is missing.
     */
    private final DefaultValue defaultValue;

    /**
     * Marks field as required.
     */
    private final Required required;

    /**
     * Custom converter for value transformation.
     */
    private final UseConverter useConverter;

    /**
     * Decryption rule for encrypted values.
     */
    private final DecryptWith decryptWith;

    /**
     * Cached constructor for the field's declared type (used for nested
     * {@code @InjectPrefix} objects). Lazily resolved and cached so it is not
     * re-looked-up via reflection on every instantiation.
     */
    private volatile Constructor<?> cachedConstructor;

    /**
     * Creates a cached metadata wrapper for a field.
     *
     * All reflection operations are performed once:
     * - Field is made accessible
     * - MethodHandles are created
     * - Annotations are cached
     * - Type metadata is stored
     */
    public FieldMeta(Field field) {

        try {

            this.field = field;
            field.setAccessible(true);

            this.setter = LOOKUP.unreflectSetter(field);
            this.getter = LOOKUP.unreflectGetter(field);

            this.isStatic = Modifier.isStatic(field.getModifiers());

            this.fieldType = field.getType();
            this.genericType = field.getGenericType();

            this.injectProperty = field.getAnnotation(InjectProperty.class);
            this.injectPrefix = field.getAnnotation(InjectPrefix.class);
            this.injectList = field.getAnnotation(InjectList.class);

            this.defaultValue = field.getAnnotation(DefaultValue.class);
            this.required = field.getAnnotation(Required.class);

            this.useConverter = field.getAnnotation(UseConverter.class);
            this.decryptWith = field.getAnnotation(DecryptWith.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize FieldMeta for: "
                            + field.getName(),
                    e
            );
        }
    }

    /**
     * Writes a value into the field using MethodHandle.
     * <p>
     * Faster than reflection-based Field.set().
     */
    public void set(Object instance, Object value) {
        try {
            setter.invoke(instance, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a value from the field using MethodHandle.
     *
     * Used mainly for nested object retrieval.
     */
    public Object get(Object instance) {
        try {
            return getter.invoke(instance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Field getField() {
        return field;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public Type getGenericType() {
        return genericType;
    }

    public InjectProperty getInjectProperty() {
        return injectProperty;
    }

    public InjectPrefix getInjectPrefix() {
        return injectPrefix;
    }

    public InjectList getInjectList() {
        return injectList;
    }

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }

    public Required getRequired() {
        return required;
    }

    public UseConverter getUseConverter() {
        return useConverter;
    }

    public DecryptWith getDecryptWith() {
        return decryptWith;
    }

    /**
     * Returns the (cached) no-arg-or-matching declared constructor for this
     * field's type, resolving and caching it on first use.
     *
     * @param arguments constructor arguments, used to pick the right constructor arity
     * @return the resolved constructor
     * @throws NoSuchMethodException if no matching constructor exists
     */
    public Constructor<?> getOrResolveConstructor(Object... arguments) throws NoSuchMethodException {
        Constructor<?> ctor = cachedConstructor;
        if (ctor == null) {
            synchronized (this) {
                ctor = cachedConstructor;
                if (ctor == null) {
                    final Class<?>[] paramTypes = new Class<?>[arguments.length];
                    for (int i = 0; i < arguments.length; i++) {
                        paramTypes[i] = arguments[i] == null ? Object.class : arguments[i].getClass();
                    }
                    ctor = arguments.length == 0
                            ? fieldType.getDeclaredConstructor()
                            : fieldType.getDeclaredConstructor(paramTypes);
                    ctor.setAccessible(true);
                    cachedConstructor = ctor;
                }
            }
        }
        return ctor;
    }
}