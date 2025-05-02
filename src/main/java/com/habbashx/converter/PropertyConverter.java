package com.habbashx.converter;

public interface PropertyConverter<T> {
    T convert(String value);
}
