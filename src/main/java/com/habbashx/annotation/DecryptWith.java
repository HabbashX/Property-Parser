package com.habbashx.annotation;

import com.habbashx.decryptor.PropertyDecryptor;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to specify a custom decryption mechanism for a field within a class.
 * This annotation is used to indicate that the value of the annotated field should be
 * decrypted using a specified implementation of the {@link PropertyDecryptor} interface.
 * The decryption process is applied before any conversion or default parsing of the value.
 *
 * Fields annotated with {@code @DecryptWith} must provide a class implementing the
 * {@link PropertyDecryptor} interface, which will handle the decryption logic.
 *
 * This annotation is typically used in scenarios where sensitive or encrypted
 * configuration properties need to be securely transformed before being used in an application.
 *
 * The decryption process is invoked during runtime, and the provided implementation
 * of {@code PropertyDecryptor} will be responsible for transforming the encrypted value
 * into its decrypted form.
 *
 * Example usage may involve injecting encrypted configuration values into fields
 * of an object, where the decryption logic is applied dynamically based on the annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecryptWith {
    @NotNull Class<? extends PropertyDecryptor> value();
}
