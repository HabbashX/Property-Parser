package com.habbashx.injector;

import com.habbashx.annotation.DefaultValue;
import com.habbashx.annotation.InjectList;
import com.habbashx.annotation.InjectPrefix;
import com.habbashx.annotation.UseConverter;
import com.habbashx.annotation.DecryptWith;

import com.habbashx.annotation.InjectProperty;
import com.habbashx.annotation.Required;
import com.habbashx.converter.PropertyConverter;
import com.habbashx.decryptor.PropertyDecryptor;
import com.habbashx.parser.DataTypeParser;
import com.habbashx.parser.ListParser;

import com.habbashx.resolver.ExternalPropertyResolver;
import com.habbashx.resolver.PlaceholderResolver;
import com.habbashx.resolver.Resolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class PropertyInjector {

    private final Properties properties;
    private final File file;

    /**
     * A resolver instance used to process and replace placeholders within property values.
     * This resolver operates by identifying placeholders in the format `${key}` and replacing
     * them with corresponding values from a set of defined properties. It prioritizes replacement
     * in the following order:
     * 1. Value from provided `Properties` object.
     * 2. System property value matching the key.
     * 3. Environment variable matching the key.
     * If no replacement is found, the original placeholder `${key}` remains unchanged.
     *
     * This field is immutable and an instance of {@code PlaceholderResolver}, which extends
     * the base {@code Resolver} type.
     */
    private final Resolver placeholderResolver = new PlaceholderResolver();
    /**
     * A final instance of the {@code Resolver} type used to resolve external properties.
     * This variable is initialized with an instance of {@code ExternalPropertyResolver},
     * which provides the logic for managing or retrieving properties from external sources.
     * It is immutable once assigned and ensures consistent external property resolution across usage.
     */
    private final Resolver externalPropertyResolver = new ExternalPropertyResolver();

    /**
     * Constructs a new instance of the PropertyInjector class using the specified file.
     * This constructor initializes a new {@link Properties} object and loads properties from the file.
     *
     * @param file The file containing the property definitions to be loaded.
     *             It is expected that the file exists and is readable. If the file cannot be read or does not exist,
     *             a runtime exception will be thrown.
     */
    public PropertyInjector(File file) {
        this(new Properties(),file);
    }

    /**
     * Constructs a PropertyInjector instance that reads properties from the specified file
     * and initializes the provided Properties object.
     *
     * @param properties the Properties object to be initialized and populated with values
     *                   loaded from the file
     * @param file       the File object representing the source file containing the properties
     */
    public PropertyInjector(Properties properties,File file) {
        this.properties = properties;
        this.file = file;
        loadProperties();
    }


    /**
     * Injects field values into the specified target object by analyzing its fields marked
     * with specific annotations and resolving the respective properties or derived values.
     *
     * This method processes fields annotated with {@code @InjectProperty}, {@code @InjectPrefix},
     * and {@code @InjectList}. It uses the annotations to determine how to inject values,
     * resolves them accordingly, and sets their corresponding values for the fields,
     * taking into consideration the annotations' behavior.
     *
     * @param targetObject The object whose fields should be injected with resolved properties.
     *                     This parameter cannot be null.
     * @param arguments    Additional optional arguments that may be used during the processing
     *                     of nested objects or complex injection scenarios.
     *                     Can include contextual data required for some fields based on their annotations.
     *
     * @throws RuntimeException If an error occurs during field injection, such as an inaccessible field
     *                          or data parsing issues.
     */
    public void inject(@NotNull Object targetObject,Object... arguments) {
        try {
            Class<?> clazz = targetObject.getClass();

            for (Field field : clazz.getDeclaredFields()) {

                field.setAccessible(true);
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                Object instance = isStatic ? null : targetObject;

                if (field.isAnnotationPresent(InjectProperty.class)) {
                    injectProperty(instance,field);
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
     * @throws IllegalAccessException If the field is inaccessible or cannot be modified.
     * @throws IllegalArgumentException If the property is required but missing in the configuration.
     */
    public void injectProperty(Object instance, @NotNull Field field) throws IllegalAccessException {

        InjectProperty injectProperty = field.getAnnotation(InjectProperty.class);

        String rawValue = properties.getProperty(injectProperty.value());

        if (rawValue == null) {
            if (field.isAnnotationPresent(DefaultValue.class)) {
                rawValue = field.getAnnotation(DefaultValue.class).value();
            } else {
                if (field.isAnnotationPresent(Required.class)) {
                    throw new IllegalArgumentException("missing required property: " + injectProperty.value());
                }
            }
            if (rawValue == null) {
                return;
            }
        }

        rawValue = placeholderResolver.resolve(rawValue, properties);
        inject(field, instance, rawValue, externalPropertyResolver);

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
            InjectPrefix injectPrefix = targetField.getAnnotation(InjectPrefix.class);
            Object nestedTarget = getOrCreate(targetField.getType(),targetField,targetObject,arguments);
            String prefix = injectPrefix.value();
            for (Field nestedField : nestedTarget.getClass().getDeclaredFields()) {

                boolean isStatic = Modifier.isStatic(nestedField.getModifiers());
                Object instance = isStatic ? null:nestedTarget;

                if (nestedField.isAnnotationPresent(InjectProperty.class)) {
                    nestedField.setAccessible(true);

                    InjectProperty injectProperty = nestedField.getAnnotation(InjectProperty.class);
                    String property = prefix+"."+injectProperty.value();
                    String rawValue = properties.getProperty(property);


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
                    inject(nestedField, instance, rawValue, placeholderResolver);
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
     * @param nestedField        The field in the target object to be injected with the resolved value.
     *                           This parameter must not be null and may have additional annotations
     *                           such as {@code @DecryptWith} or {@code @UseConverter} that affect
     *                           the processing of the raw value.
     * @param instance           The object instance into which the resolved value is to be injected.
     *                           This parameter must not be null.
     * @param rawValue           The raw property value to be processed and injected into the field.
     *                           It may be resolved using a placeholder resolver or transformed
     *                           based on the field annotations.
     * @param placeholderResolver The resolver used to resolve placeholders or process the raw value
     *                            before any other steps. This must be a valid implementation of {@code Resolver}.
     *
     * @throws IllegalAccessException If the specified field cannot be written to, such as when it's inaccessible
     *                                or final.
     */
    private void inject(Field nestedField, Object instance, String rawValue, Resolver placeholderResolver) throws IllegalAccessException {
        rawValue = placeholderResolver.resolve(rawValue,properties);

        if (nestedField.isAnnotationPresent(DecryptWith.class)) {
            rawValue = decryptWith(nestedField,rawValue);
        }
        Object convertedValue;

        if (nestedField.isAnnotationPresent(UseConverter.class)) {
            convertedValue = useConverter(nestedField,rawValue);
        } else if (nestedField.getType().isEnum()) {
            convertedValue = DataTypeParser.parseEnum((Class<Enum<?>>) nestedField.getType(),rawValue);
        } else {
            convertedValue = DataTypeParser.parse(nestedField.getType(), rawValue);
        }
        nestedField.set(instance,convertedValue);
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
        InjectList injectList = field.getAnnotation(InjectList.class);
        Type genericsType = field.getGenericType();

        if (genericsType instanceof ParameterizedType parameterizedType) {
            Type paramType = parameterizedType.getActualTypeArguments()[0];
            String rawValue = properties.getProperty(injectList.value());
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
    @SuppressWarnings("unchecked")
    private String decryptWith(@NotNull Field field , String encryptedValue) {

        try {
            DecryptWith decryptWith = field.getAnnotation(DecryptWith.class);

            Constructor<? extends DecryptWith> constructor = (Constructor<? extends DecryptWith>) decryptWith.value().getDeclaredConstructor();
            constructor.setAccessible(true);

            PropertyDecryptor propertyDecryptor = (PropertyDecryptor) constructor.newInstance();

            return propertyDecryptor.decrypt(encryptedValue);
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
    @SuppressWarnings("unchecked")
    private Object useConverter(@NotNull Field field ,String rawValue) {

        try {
            final UseConverter converter = field.getAnnotation(UseConverter.class);

            Constructor<? extends UseConverter> constructor = (Constructor<? extends UseConverter>) converter.value().getDeclaredConstructor();
            constructor.setAccessible(true);

            PropertyConverter<?> propertyConverter = (PropertyConverter<?>) constructor.newInstance();

            return propertyConverter.convert(rawValue);
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

    /**
     * Stores a key-value property with an optional comment into a properties file.
     *
     * @param key        the key to be stored in the properties file
     * @param newValue   the value associated with the specified key
     * @param comment    a comment to include in the properties file, can be null
     */
    private void store(String key,String newValue ,String comment){
        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.setProperty(key, newValue);
            properties.store(outputStream, comment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads properties from a file into the {@code properties} object.
     *
     * This method attempts to open the file specified by the {@code file}
     * field and read its contents into the {@code properties} object.
     * If the file cannot be opened or read due to an I/O error, a
     * {@code RuntimeException} is thrown encapsulating the original exception.
     *
     * The method ensures that the input stream is properly closed after
     * the properties are loaded, using a try-with-resources block.
     *
     * Throws:
     * - {@code RuntimeException} if an {@code IOException} occurs while
     *   reading the properties file.
     */
    public void loadProperties() {
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
