package com.habbashx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that the annotated field is required to have a value.
 * This annotation can be used for marking fields where a property value must be
 * provided either through external configuration, default values, or property resolvers.
 *
 * If the field annotated with this annotation is missing a value during processing,
 * an {@code IllegalArgumentException} will be thrown.
 *
 * This annotation is typically used in conjunction with property injection mechanisms.
 *
 * It should be applied only to fields.
 *
 * @see Retention
 * @see Target
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {
}
