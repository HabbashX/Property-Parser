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

    private final Resolver placeholderResolver = new PlaceholderResolver();
    private final Resolver externalPropertyResolver = new ExternalPropertyResolver();

    public PropertyInjector(File file) {
        this(new Properties(),file);
    }

    public PropertyInjector(Properties properties,File file) {
        this.properties = properties;
        this.file = file;
        loadProperties();
    }


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
        rawValue = externalPropertyResolver.resolve(rawValue,properties);

        if (field.isAnnotationPresent(DecryptWith.class)) {
            rawValue = decryptWith(field, rawValue);
        }

        Object convertedValue;

        if (field.isAnnotationPresent(UseConverter.class)) {
            convertedValue = useConverter(field, rawValue);
        } else if (field.getType().isEnum()) {
            convertedValue = DataTypeParser.parseEnum((Class<Enum<?>>) field.getType(), rawValue);
        } else {
            convertedValue = DataTypeParser.parse(field.getType(), rawValue);
        }

        field.set(instance, convertedValue);

    }
    public void injectFieldWithPrefix(Object targetObject ,Object... arguments) {
        inject(targetObject,arguments);
    }

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
                } else if (nestedField.isAnnotationPresent(InjectList.class)) {
                    injectList(instance,nestedField);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    private void store(String key,String newValue ,String comment){
        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.setProperty(key, newValue);
            properties.store(outputStream, comment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadProperties() {
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
