package com.habbashx.injector;

import com.habbashx.annotation.InjectList;
import com.habbashx.annotation.InjectPrefix;
import com.habbashx.annotation.UseConverter;
import com.habbashx.annotation.DecryptWith;

import com.habbashx.annotation.InjectProperty;
import com.habbashx.converter.PropertyConverter;
import com.habbashx.converter.registry.PropertyConverterRegistry;
import com.habbashx.decryptor.PropertyDecryptor;
import com.habbashx.decryptor.registry.PropertyDecryptorRegistry;
import com.habbashx.injector.meta.FieldMeta;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PropertyInjector is a high-performance annotation-driven dependency injection engine
 * designed for injecting configuration values into Java objects at runtime.
 *
 * <p>This system resolves values from a {@link PropertySource}, applies optional
 * transformation pipelines (resolvers, decryptors, converters), and injects them
 * into fields using reflection optimized with caching and MethodHandles.</p>
 *
 * <h2>Core Responsibilities</h2>
 * <ul>
 *     <li>Resolve configuration values from a PropertySource</li>
 *     <li>Support nested object injection via {@link InjectPrefix}</li>
 *     <li>Support list parsing via {@link InjectList}</li>
 *     <li>Support custom conversion via {@link UseConverter}</li>
 *     <li>Support encrypted values via {@link DecryptWith}</li>
 * </ul>
 *
 * <h2>Performance Design</h2>
 * <ul>
 *     <li>Field metadata is cached per class to avoid repeated reflection calls</li>
 *     <li>Setter/getter operations use {@link java.lang.invoke.MethodHandle} instead of Field.set</li>
 *     <li>Annotation lookups are precomputed in {@link FieldMeta}</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is thread-safe for injection operations assuming:
 * PropertySource and registries are thread-safe implementations.</p>
 */
public class PropertyInjector {

    /**
     * Source of raw configuration values.
     */
    private final PropertySource propertySource;

    /** Registry for converting raw strings into custom types */
    private final PropertyConverterRegistry propertyConverterRegistry;

    /** Registry for decrypting encrypted property values */
    private final PropertyDecryptorRegistry propertyDecryptorRegistry;
    /** Pipeline registry for resolving placeholders and external values */
    private final ResolverRegistry resolverRegistry;

    /**
     * Cache of class-level field metadata.
     * Prevents repeated reflection operations.
     */
    private final Map<Class<?>,List<FieldMeta>> metadataCache = new ConcurrentHashMap<>();

    /**
     * Cache of individual field metadata, keyed by {@link Field}.
     * This backs {@link #getFieldMeta(Field)} so that every call site
     * (injectProperty, inject, injectList, decryptWith, useConverter,
     * getOrCreate) reuses the same {@link FieldMeta} instance instead of
     * re-resolving MethodHandles and annotations on every injection.
     */
    private final Map<Field, FieldMeta> fieldMetaCache = new ConcurrentHashMap<>();


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
                new ResolverRegistry()
        );
    }

    /**
     * Full constructor with dependency injection.
     *
     * @param propertySource property source implementation
     * @param propertyConverterRegistry converter registry
     * @param propertyDecryptorRegistry decryptor registry
     * @param resolverRegistry resolver pipeline registry
     */
    public PropertyInjector(PropertySource propertySource,
                            PropertyConverterRegistry propertyConverterRegistry,
                            PropertyDecryptorRegistry propertyDecryptorRegistry,
                            ResolverRegistry resolverRegistry
    ) {
        this.propertySource = propertySource;
        this.propertyConverterRegistry = propertyConverterRegistry;
        this.propertyDecryptorRegistry = propertyDecryptorRegistry;
        this.resolverRegistry = resolverRegistry;
    }

    /**
     * Injects all annotated fields into the target object.
     *
     * <p>This method scans cached metadata and delegates injection
     * based on annotation type.</p>
     *
     * @param targetObject object to inject into
     * @param arguments optional constructor arguments for nested objects
     *
     * @throws RuntimeException if injection fails
     */
    public void inject(@NotNull Object targetObject, Object... arguments) {
        try {

            final List<FieldMeta> metas = getFieldMetas(targetObject.getClass());


            for (final FieldMeta meta : metas) {

                final boolean isStatic = Modifier.isStatic(meta.getField().getModifiers());
                final Object instance = isStatic ? null : targetObject;

                if (meta.getInjectProperty() != null) {
                    injectProperty(instance, meta.getField());
                } else if (meta.getInjectPrefix() != null) {
                    injectNestedProperties(targetObject, meta.getField(), arguments);
                } else if (meta.getInjectList() != null) {
                    injectList(instance, meta.getField());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Injects a single property value into a field.
     *
     * <p>Resolution order:
     * PropertySource → DefaultValue → Required validation</p>
     */
    public void injectProperty(Object instance, @NotNull Field field) throws IllegalAccessException {

        final FieldMeta fieldMeta = getFieldMeta(field);
        final InjectProperty injectProperty = fieldMeta.getInjectProperty();

        String rawValue = propertySource.get(injectProperty.value());

        if (rawValue == null) {
            if (fieldMeta.getDefaultValue() != null) {
                rawValue = fieldMeta.getDefaultValue().value();
            } else if (fieldMeta.getRequired() != null) {
                throw new IllegalArgumentException("missing required property: " + injectProperty.value());
            }
        }
        if (rawValue == null) {
            return;
        }
        inject(field, instance, rawValue);
    }


    /**
     * Injects nestedField values with a prefix into the specified target object. It leverages the `inject` method
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
     * Injects nested properties into the given target nestedField of a target object. This method analyzes the
     * nestedField's annotations to determine how to resolve property values and process complex cases such as
     * nested objects, lists, or annotated fields. It sets the resolved and parsed values into the respective
     * nestedField instances, applying conversions, decryption, and default values as needed.
     *
     * @param targetObject The parent object that contains the target nestedField into which nested properties
     *                     are to be injected. This object should already have its parent-level context
     *                     properly initialized.
     * @param targetField  The nestedField within the targetObject that represents a nested object. The nestedField
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
            final FieldMeta targetMeta = getFieldMeta(targetField);
            final InjectPrefix injectPrefix = targetMeta.getInjectPrefix();
            final Object nestedTarget = getOrCreate(targetField.getType(),targetField,targetObject,arguments);
            final String prefix = injectPrefix.value();

            final List<FieldMeta> nestedMetas = getFieldMetas(targetField.getType());
            for (final FieldMeta meta : nestedMetas) {

                final Field nestedField = meta.getField();

                final boolean isStatic = Modifier.isStatic(nestedField.getModifiers());
                final Object instance = isStatic ? null:nestedTarget;

                if (meta.getInjectProperty() != null) {

                    final String property = prefix+"."+meta.getInjectProperty().value();
                    String rawValue = propertySource.get(property);


                    if (rawValue == null) {
                        if (meta.getDefaultValue() != null) {
                            rawValue = meta.getDefaultValue().value();
                        } else {
                            if (meta.getRequired() != null) {
                                throw new IllegalArgumentException("missing required property: " + meta.getInjectProperty().value());
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
     * Injects a resolved and potentially transformed value into the specified nestedField of the given instance.
     * The input value is processed through optional decryption, conversion, or default parsing based on
     * the annotations and type of the nestedField.
     *
     * @param field        The nestedField in the target object to be injected with the resolved value.
     *                           This parameter must not be null and may have additional annotations
     *                           such as {@code @DecryptWith} or {@code @UseConverter} that affect
     *                           the processing of the raw value.
     * @param instance           The object instance into which the resolved value is to be injected.
     *                           This parameter must not be null.
     * @param rawValue           The raw property value to be processed and injected into the nestedField.
     *                           It may be resolved using a placeholder resolver or transformed
     *                           based on the nestedField annotations.
     * @throws IllegalAccessException If the specified nestedField cannot be written to, such as when it's inaccessible
     *                                or final.
     */
    private void inject(@NotNull Field field, Object instance, String rawValue) throws IllegalAccessException {


        final FieldMeta fieldMeta = getFieldMeta(field);

        rawValue = resolverRegistry.resolve(rawValue,propertySource.getAll());

        if (fieldMeta.getDecryptWith() != null) {
            rawValue = decryptWith(field,rawValue);
        }

        Object convertedValue;

        if (fieldMeta.getUseConverter() != null) {
            convertedValue = useConverter(field,rawValue);
        } else {
            convertedValue = ParserFactory.parse(field.getType(),rawValue);
        }
        fieldMeta.set(instance,convertedValue);
    }

    /**
     * Optimized injectList()
     */
    private void injectList(
            Object instance,
            @NotNull Field field
    ) {

        final FieldMeta meta =
                getFieldMeta(field);

        final InjectList injectList =
                meta.getInjectList();

        final Type genericType =
                field.getGenericType();

        if (genericType instanceof ParameterizedType p) {

            final Type paramType =
                    p.getActualTypeArguments()[0];

            final String rawValue =
                    propertySource.get(
                            injectList.value()
                    );

            if (rawValue == null) {
                return;
            }

            final List<Object> list =
                    ListParser.parseList(
                            rawValue,
                            paramType
                    );

            meta.set(instance, list);
        }
    }

    private String decryptWith(
            @NotNull Field field,
            String encryptedValue
    ) {

        try {

            final FieldMeta meta =
                    getFieldMeta(field);

            return propertyDecryptorRegistry.decrypt(
                    meta.getDecryptWith().value(),
                    encryptedValue
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object useConverter(
            @NotNull Field field,
            String rawValue) {

        try {

            final FieldMeta meta =
                    getFieldMeta(field);

            return propertyConverterRegistry.convert(
                    meta.getUseConverter().value(),
                    field.getType(),
                    rawValue
            );

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Optimized getOrCreate()
     */
    private @NotNull Object getOrCreate(
            Class<?> clazz,
            Field field,
            Object target,
            Object... arguments
    ) {

        try {

            final FieldMeta meta =
                    getFieldMeta(field);
            Object instance =
                    meta.get(target);
            if (instance == null) {

                instance =
                        meta.getOrResolveConstructor(arguments)
                                .newInstance(arguments);

                meta.set(target, instance);
            }

            return instance;

        } catch (Throwable e) {

            throw new RuntimeException(
                    "Failed to get or create instance for field: "
                            + field.getName(),
                    e
            );
        }
    }

    @Contract("_ -> !null")
    private @NotNull FieldMeta getFieldMeta(final Field field) {
        // IMPORTANT: field.getDeclaringClass() may not equal the class we cached
        // metadata under (e.g. for nested targets), so we key a dedicated
        // Field -> FieldMeta cache directly, which is always correct and O(1).
        return fieldMetaCache.computeIfAbsent(field, FieldMeta::new);
    }
    private List<FieldMeta> getFieldMetas(
            Class<?> clazz
    ) {

        return metadataCache.computeIfAbsent(
                clazz,
                c -> {

                    final List<FieldMeta> metas =
                            new ArrayList<>();

                    for (Field field :
                            c.getDeclaredFields()) {

                        field.setAccessible(true);

                        final FieldMeta meta = fieldMetaCache.computeIfAbsent(field, FieldMeta::new);

                        metas.add(meta);
                    }

                    return metas;
                }
        );
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
                    resolverRegistry
            );
        }
    }
}