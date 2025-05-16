package com.habbashx.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code DefaultValue} annotation is used to specify a default value for a field when no explicit
 * value is provided through other configurations or property files. This annotation can be applied to
 * fields where a default fallback is required, ensuring the field is not left uninitialized.
 *
 * Fields annotated with {@code DefaultValue} are typically referenced in conjunction with property
 * injection frameworks. If a value is not provided from the external configuration or property source,
 * the value specified in this annotation is used as the default.
 *
 * This annotation is designed for use on fields within a class and should be combined with proper
 * property resolution logic to enable the desired behavior.
 *
 * Retention policy: {@code RUNTIME} to allow runtime access via reflection.
 * Applicable target: {@code FIELD} to ensure it applies only to fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {
  @NotNull String value();
}
