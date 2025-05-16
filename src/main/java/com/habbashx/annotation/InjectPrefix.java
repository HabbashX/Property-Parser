package com.habbashx.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields for injection of prefixed values.
 *
 * When a field is annotated with {@code @InjectPrefix}, its value will be automatically
 * injected with a specific prefix derived from the annotation's {@code value()} attribute.
 * The annotation is typically used in environments that require injecting
 * hierarchical or nested configurations, allowing fields to receive values prefixed by
 * the specified identifier.
 *
 * This annotation is retained at runtime and applies exclusively to fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectPrefix {
    @NotNull String value();
}
