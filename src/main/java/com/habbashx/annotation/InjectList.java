package com.habbashx.annotation;


import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to specify that a list of values should be injected into the annotated field.
 *
 * The {@code InjectList} annotation is used to mark a field for injection with a list of values
 * based on the configuration or context. A string value is specified, typically serving as
 * a key or identifier to resolve the list of values to be injected.
 *
 * The processing of this annotation typically occurs during runtime, where the field's value
 * is dynamically determined and assigned based on the provided {@code value}.
 *
 * Fields annotated with this annotation are expected to work with relevant injection
 * mechanisms that interpret and resolve the specified value.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectList {
    @NotNull String value();
}
