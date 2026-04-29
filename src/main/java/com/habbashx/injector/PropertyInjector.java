package com.habbashx.injector;

import com.habbashx.annotation.DefaultValue;
import com.habbashx.annotation.InjectList;
import com.habbashx.annotation.InjectPrefix;
import com.habbashx.annotation.UseConverter;
import com.habbashx.annotation.DecryptWith;

import com.habbashx.annotation.InjectProperty;
import com.habbashx.annotation.Required;
import com.habbashx.converter.PropertyConverter;
import com.habbashx.converter.registry.PropertyConverterRegistry;
import com.habbashx.decryptor.PropertyDecryptor;
import com.habbashx.decryptor.registry.PropertyDecryptorRegistry;
import com.habbashx.injector.reflection.cache.ReflectionCache;
import com.habbashx.injector.source.FilePropertySource;
import com.habbashx.injector.source.PropertySource;
import com.habbashx.parser.ListParser;

import com.habbashx.parser.factory.ParserFactory;
import com.habbashx.resolver.Resolver;
import com.habbashx.resolver.registry.ResolverRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

/**
 * <h1>PropertyInjector</h1>
 *
 * A lightweight annotation-based property injection engine.
 *
 * <p>This class scans target objects using reflection and injects values
 * from a property source into fields using annotations such as:</p>
 *
 * <ul>
 *     <li>{@link InjectProperty}</li>
 *     <li>{@link InjectPrefix}</li>
 *     <li>{@link InjectList}</li>
 *     <li>{@link UseConverter}</li>
 *     <li>{@link DecryptWith}</li>
 * </ul>
 *
 * <p>It supports:</p>
 * <ul>
 *     <li>Nested object injection</li>
 *     <li>Custom type converters</li>
 *     <li>Decryptors</li>
 *     <li>Resolver pipeline</li>
 *     <li>List parsing</li>
 * </ul>
 */
public class PropertyInjector {

    /** Source of all property values (file, memory, etc.) */
    private final PropertySource propertySource;

    /** Registry for converting raw strings into custom types */
    private final PropertyConverterRegistry propertyConverterRegistry;

    /** Registry for decrypting encrypted property values */
    private final PropertyDecryptorRegistry propertyDecryptorRegistry;

    /** Cache for reflection fields to improve performance */
    private final ReflectionCache reflectionCache;

    /** Pipeline registry for resolving placeholders and external values */
    private final ResolverRegistry resolverRegistry;

    /**
     * Creates a PropertyInjector using a property file.
     *
     * @param file property file source
     */
    public PropertyInjector(File file) {
        this(
                new FilePropertySource(file),
                new PropertyConverterRegistry(),
                new PropertyDecryptorRegistry(),
                new ReflectionCache(),
                new ResolverRegistry()
        );
    }

    /**
     * Full constructor with dependency injection.
     *
     * @param propertySource property source implementation
     * @param propertyConverterRegistry converter registry
     * @param propertyDecryptorRegistry decryptor registry
     * @param reflectionCache cached reflection access
     * @param resolverRegistry resolver pipeline registry
     */
    public PropertyInjector(PropertySource propertySource,
                            PropertyConverterRegistry propertyConverterRegistry,
                            PropertyDecryptorRegistry propertyDecryptorRegistry,
                            ReflectionCache reflectionCache,
                            ResolverRegistry resolverRegistry
    ) {
        this.propertySource = propertySource;
        this.propertyConverterRegistry = propertyConverterRegistry;
        this.propertyDecryptorRegistry = propertyDecryptorRegistry;
        this.reflectionCache = reflectionCache;
        this.resolverRegistry = resolverRegistry;
    }

    /**
     * Injects values into the given target object.
     *
     * <p>Scans all fields and applies injection rules based on annotations.</p>
     *
     * @param targetObject object to inject values into
     * @param arguments optional arguments for nested injection
     */
    public void inject(@NotNull Object targetObject, Object... arguments) {
        try {
            Class<?> clazz = targetObject.getClass();

            for (Field field : reflectionCache.getFields(clazz)) {

                field.setAccessible(true);
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                Object instance = isStatic ? null : targetObject;

                if (field.isAnnotationPresent(InjectProperty.class)) {
                    injectProperty(instance, field);
                } else if (field.isAnnotationPresent(InjectPrefix.class)) {
                    injectNestedProperties(targetObject, field, arguments);
                } else if (field.isAnnotationPresent(InjectList.class)) {
                    injectList(instance, field);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Injects a property value into the specified field of the given instance. The property value can be
     * retrieved from predefined properties, default values, or external property resolvers. Additional
     * annotations can be used to modify or parse the property value before injection.
     *
     * @param instance The object instance into which the property value is injected. Must not be null.
     * @param field    The field within the instance that will have the value injected. Must not be null.
     *                 It must be annotated with {@code InjectProperty}.
     * @throws IllegalAccessException   If the field is inaccessible or cannot be modified.
     * @throws IllegalArgumentException If the property is required but missing in the configuration.
     */
    public void injectProperty(Object instance, @NotNull Field field) throws IllegalAccessException {

        final InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);

        String rawValue = propertySource.get(injectProperty.value());

        if (rawValue == null) {
            if (field.isAnnotationPresent(DefaultValue.class)) {
                rawValue = field.getAnnotation(DefaultValue.class).value();
            } else if (field.isAnnotationPresent(Required.class)) {
                throw new IllegalArgumentException("missing required property: " + injectProperty.value());
            }
        }
        if (rawValue == null) {
            return;
        }
        inject(field, instance, rawValue);
    }


    /**
     * Injects field values with a prefix into the specified target object. It leverages the `inject` method
     * to analyze annotations and resolve property values based on the provided arguments.
     *
     * @param targetObject The object on which fields should be injected. This parameter must not be null.
     * @param arguments    Optional additional arguments that can be used during the injection process for
     *                     resolving complex or nested properties.
     */
    public void injectFieldWithPrefix(Object targetObject ,Object... arguments) {
        inject(targetObject,arguments);
    }

    /**
     * Injects nested properties into the given target field of a target object. This method analyzes the
     * field's annotations to determine how to resolve property values and process complex cases such as
     * nested objects, lists, or annotated fields. It sets the resolved and parsed values into the respective
     * field instances, applying conversions, decryption, and default values as needed.
     *
     * @param targetObject The parent object that contains the target field into which nested properties
     *                     are to be injected. This object should already have its parent-level context
     *                     properly initialized.
     * @param targetField  The field within the targetObject that represents a nested object. The field
     *                     needs to be annotated with {@code InjectPrefix} to determine the property prefix.
     * @param arguments    Additional arguments that may be required for creating or injecting nested objects.
     *                     These arguments can provide context or initialization data necessary for processing
     *                     nested properties.
     *
     * @throws RuntimeException If any error occurs during the injection process, such as accessing inaccessible
     *                          fields, missing required properties, or data parsing errors.
     */
    private void injectNestedProperties(Object targetObject,Field targetField,Object... arguments) {

        try {
            final InjectPrefix injectPrefix = targetField.getAnnotation(InjectPrefix.class);
            final Object nestedTarget = getOrCreate(targetField.getType(),targetField,targetObject,arguments);
            final String prefix = injectPrefix.value();
            for (final Field nestedField : nestedTarget.getClass().getDeclaredFields()) {

                final boolean isStatic = Modifier.isStatic(nestedField.getModifiers());
                final Object instance = isStatic ? null:nestedTarget;

                if (nestedField.isAnnotationPresent(InjectProperty.class)) {
                    nestedField.setAccessible(true);

                    final InjectProperty injectProperty = nestedField.getAnnotation(InjectProperty.class);
                    final String property = prefix+"."+injectProperty.value();
                    String rawValue = propertySource.get(property);


                    if (rawValue == null) {
                        if (nestedField.isAnnotationPresent(DefaultValue.class)) {
                            rawValue = nestedField.getAnnotation(DefaultValue.class).value();
                        } else {
                            if (nestedField.isAnnotationPresent(Required.class)) {
                                throw new IllegalArgumentException("missing required property: " + injectProperty.value());
                            } else {
                                continue;
                            }
                        }
                    }
                    inject(nestedField, instance, rawValue);
                } else if (nestedField.isAnnotationPresent(InjectList.class)) {
                    injectList(instance,nestedField);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Injects a resolved and potentially transformed value into the specified field of the given instance.
     * The input value is processed through optional decryption, conversion, or default parsing based on
     * the annotations and type of the field.
     *
     * @param field        The field in the target object to be injected with the resolved value.
     *                           This parameter must not be null and may have additional annotations
     *                           such as {@code @DecryptWith} or {@code @UseConverter} that affect
     *                           the processing of the raw value.
     * @param instance           The object instance into which the resolved value is to be injected.
     *                           This parameter must not be null.
     * @param rawValue           The raw property value to be processed and injected into the field.
     *                           It may be resolved using a placeholder resolver or transformed
     *                           based on the field annotations.
     * @throws IllegalAccessException If the specified field cannot be written to, such as when it's inaccessible
     *                                or final.
     */
    private void inject(@NotNull Field field, Object instance, String rawValue) throws IllegalAccessException {
        rawValue = resolverRegistry.resolve(rawValue,propertySource.getAll());

        if (field.isAnnotationPresent(DecryptWith.class)) {
            rawValue = decryptWith(field,rawValue);
        }

        Object convertedValue;

        if (field.isAnnotationPresent(UseConverter.class)) {
            convertedValue = useConverter(field,rawValue);
        } else {
            convertedValue = ParserFactory.parse(field.getType(),rawValue);
        }
        field.setAccessible(true);
        field.set(instance,convertedValue);
    }

    /**
     * Injects a list into the specified field of the given instance based on the {@link InjectList} annotation.
     * Parses the values from a property source and converts them to a list of the appropriate type.
     *
     * @param instance the object instance where the field is to be injected
     * @param field the field to be injected, annotated with {@link InjectList}
     * @throws IllegalAccessException if the field cannot be accessed or modified
     */
    private void injectList(Object instance,@NotNull Field field) throws IllegalAccessException {
        final InjectList injectList = field.getAnnotation(InjectList.class);
        final Type genericsType = field.getGenericType();

        if (genericsType instanceof final ParameterizedType parameterizedType) {
            final Type paramType = parameterizedType.getActualTypeArguments()[0];
            final String rawValue = propertySource.get(injectList.value());
            final List<Object> list = ListParser.parseList(rawValue,paramType);
            field.set(instance,list);
        }
    }

    /**
     * Decrypts the given encrypted value using the decryption logic specified by
     * the {@link DecryptWith} annotation on the provided field.
     *
     * This method retrieves the `DecryptWith` annotation from the field, constructs
     * an instance of the specified {@link PropertyDecryptor} implementation, and uses it
     * to decrypt the provided encrypted value.
     *
     * @param field The field annotated with {@link DecryptWith} that specifies the
     *              decryption logic. Must not be null.
     * @param encryptedValue The value to be decrypted. Can be null if the encrypted
     *                       value itself is null.
     * @return The decrypted value as a string. If the decryption is unsuccessful or an
     *         error is encountered, a runtime exception is thrown.
     * @throws RuntimeException If any exception occurs during the decryption process,
     *                          such as reflection-related issues or instantiation problems.
     */
    @Contract(pure = true)
    private String decryptWith(@NotNull Field field , String encryptedValue) {

        try {
            final DecryptWith decryptWith = field.getAnnotation(DecryptWith.class);
            return propertyDecryptorRegistry.decrypt(decryptWith.value(),encryptedValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Converts the provided raw value using a custom {@link PropertyConverter} specified by the
     * {@link UseConverter} annotation on the given field.
     *
     * This method retrieves the {@link UseConverter} annotation from the field, creates an instance
     * of the specified {@code PropertyConverter} implementation, and invokes its {@code convert}
     * method to process the raw value into the appropriate type.
     *
     * @param field The field annotated with {@link UseConverter} that specifies the
     *              {@code PropertyConverter} class to be used. Must not be null.
     * @param rawValue The raw value to be converted. This can be null or a string value
     *                 that will be passed to the {@code convert} method of the
     *                 {@code PropertyConverter}.
     * @return The converted object, as returned by the {@code convert} method of the
     *         {@code PropertyConverter}. If any exception occurs during the conversion,
     *         a {@link RuntimeException} is thrown.
     * @throws RuntimeException If any error occurs during the reflection process, instance creation,
     *                          or conversion, such as a missing or inaccessible annotation,
     *                          invalid constructor, or unexpected exception in the converter.
     */
    @Contract(pure = true)
    private Object useConverter(@NotNull Field field ,String rawValue) {

        try {
            final UseConverter converter = field.getAnnotation(UseConverter.class);

            return propertyConverterRegistry.convert(converter.value(),field.getType(),rawValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the existing instance from the specified field of the target object or,
     * if it is null, creates a new instance of the provided class with optional arguments.
     * The instance is then stored back into the specified field.
     *
     * @param clazz The class to be instantiated if the field's current value is null.
     *              This parameter must not be null.
     * @param field The field from which the instance will be retrieved or where the new instance
     *              will be stored. This field will be made accessible temporarily regardless of
     *              its original access modifier. Must not be null.
     * @param target The target object containing the specified field. Must not be null.
     * @param arguments Optional arguments to pass to the constructor of the class when creating
     *                  a new instance. Can be empty if no arguments are required.
     *
     * @return The existing or newly created and stored instance. The returned object is guaranteed
     *         not to be null.
     *
     * @throws RuntimeException If any exception occurs during reflection, such as when accessing
     *                          the field or creating a new instance.
     */
    private @NotNull Object getOrCreate(Class<?> clazz,Field field ,Object target,Object... arguments) {
        try {
            field.setAccessible(true);
            Object instance = field.get(target);

            if (instance == null) {
                instance = clazz.getDeclaredConstructor().newInstance(arguments);
            }

            field.set(target,instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(" -> new")
    public static @NotNull PropertyInjectorBuilder injectorBuilder() {
        return new PropertyInjectorBuilder();
    }


    /**
     * Builder for configuring PropertyInjector.
     */
    public static class PropertyInjectorBuilder {

        private PropertySource propertySource;

        private final PropertyConverterRegistry propertyConverterRegistry = new PropertyConverterRegistry();
        private final PropertyDecryptorRegistry propertyDecryptorRegistry = new PropertyDecryptorRegistry();
        private final ResolverRegistry resolverRegistry = new ResolverRegistry();

        /** Sets property source file */
        public PropertyInjectorBuilder propertySource(File file) {
            propertySource = new FilePropertySource(file);
            return this;
        }

        /** Registers converter */
        public <T> PropertyInjectorBuilder propertyConverter(Class<? extends PropertyConverter<T>> type , PropertyConverter<T> propertyConverter) {
            propertyConverterRegistry.register(type,propertyConverter);
            return this;
        }

        /** Registers decryptor */
        public <T> PropertyInjectorBuilder propertyDecryptor(Class<? extends PropertyDecryptor> type,PropertyDecryptor propertyDecryptor) {
            propertyDecryptorRegistry.register(type,propertyDecryptor);
            return this;
        }

        /** Registers resolver */
        public PropertyInjectorBuilder resolver(Resolver resolver) {
            resolverRegistry.register(resolver);
            return this;
        }

        /** Builds injector */
        public PropertyInjector build() {
            return new PropertyInjector(
                    propertySource,
                    propertyConverterRegistry,
                    propertyDecryptorRegistry,
                    new ReflectionCache(),
                    resolverRegistry
            );
        }
    }
}

