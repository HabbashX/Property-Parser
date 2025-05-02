package com.habbashx.annotation;

import com.habbashx.converter.PropertyConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UseConverter {
    @NotNull Class<? extends PropertyConverter<?>> value();
}
