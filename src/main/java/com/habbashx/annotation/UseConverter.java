package com.habbashx.annotation;

import com.habbashx.converter.PropertyConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a custom converter for transforming raw property values into the appropriate type
 * for a given field. This enables custom logic to process and convert property values at runtime.
 *
 * The annotated field must indicate the class of the converter to be used for transformation,
 * which should implement the {@code PropertyConverter} interface.
 *
 * This annotation is typically used in scenarios where default data type parsing is insufficient,
 * and a specific conversion logic is required to process the raw property value.
 *
 * Fields annotated with {@code @UseConverter} are processed by extracting the specified converter class,
 * invoking the converter's {@code convert} method with the raw value, and injecting the transformed value
 * into the field.
 *
 * Usage requires providing a class that extends {@code PropertyConverter<?>} through the {@code value} element.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UseConverter {
    @NotNull Class<? extends PropertyConverter<?>> value();
}
