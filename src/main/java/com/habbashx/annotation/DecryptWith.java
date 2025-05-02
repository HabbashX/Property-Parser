package com.habbashx.annotation;

import com.habbashx.decryptor.PropertyDecryptor;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecryptWith {
    @NotNull Class<? extends PropertyDecryptor> value();
}
