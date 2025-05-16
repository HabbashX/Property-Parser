package com.habbashx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code InjectProperty} annotation is used to mark a field for automatic
 * dependency injection of a property value. The value of the property to be
 * injected is specified through the annotation's {@code value} element.
 *
 * This annotation is typically used in a custom dependency injection or property
 * management framework that identifies fields annotated with {@code InjectProperty}
 * and assigns them values based on the configured properties or resources.
 *
 * Annotation usage is usually paired with reflection-based mechanisms to process
 * the annotation and perform the desired field injection. The injected value is
 * typically derived from an underlying configuration source, such as a
 * properties file, environment variables, or another external source.
 *
 * Example scenarios include injecting application configuration values,
 * environment-specific settings, or dynamic resource values directly into
 * fields of an object.
 *
 * This annotation must be applied at the field level and will be retained
 * during runtime to allow reflective access.
 *
 * Annotation properties:
 * - {@code value}: The identifier or key representing the property value
 *   that should be injected into the annotated field.
 *
 * Target:
 * - This annotation can only be used on fields.
 *
 * Retention:
 * - The annotation is retained at runtime and is therefore accessible
 *   during program execution via reflection.
 *
 * See also: Custom injection methods or frameworks capable of processing
 * the {@code InjectProperty} annotation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectProperty {
   String value();
}
