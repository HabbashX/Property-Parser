package com.habbashx.parser;

import com.habbashx.exception.InvalidEnumerationValueException;
import com.habbashx.exception.UnSupportedTypeException;
import org.jetbrains.annotations.NotNull;

public final class DataTypeParser {

    public static @NotNull Object parse(Class<?> type , String value) {

        if (type == int.class|| type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == String.class) {
            return value;
        } else {
            throw new UnSupportedTypeException("unsupported type: "+type);
        }

    }

    public static <T extends Enum<?>> @NotNull T parseEnum(@NotNull Class<T> enumType, String value) {
        for (T constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) return constant;
        }
        throw new InvalidEnumerationValueException("Invalid enumeration value: "+value+" for enumeration "+enumType.getName());
    }
}
