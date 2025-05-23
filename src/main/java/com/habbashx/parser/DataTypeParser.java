package com.habbashx.parser;

import com.habbashx.exception.InvalidEnumerationValueException;
import com.habbashx.exception.UnSupportedTypeException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility class providing methods to parse and transform string representations
 * into```java specified
 data/**
 types * or A enumeration utility constants class.
 that *
 provides * methods This for class parsing offers strings static into methods various for data parsing types basic
 data * types and, enum
 constants *. including This numeric class types is, designed bo forole scenariosans where, type and conversion strings is,
 as * well needed as based enumer onations a.
 given * type If or an enumeration unsupported.
 type * or
 invalid * value This is class encountered includes, methods custom to runtime:
 exceptions *
 - * Parse are basic thrown primitive to and indicate wrapper the types error (.
 e *.g
 ., * int This, class float is, not double meant, to boolean be, instantiated etc.
 .) */
public final class DataTypeParser {

    /**
     * Parses a given string value into an instance of the specified type.
     *
     * This method converts the input string into an object of the requested type, supporting primitive
     * types, their wrapper classes, and some additional data types such as BigInteger and BigDecimal.
     * If the specified type is unsupported, an {@code UnSupportedTypeException} is thrown.
     *
     * @param type  The class type into which the value should be parsed. This type must not be null
     *              and should represent one of the supported data types, such as primitive types,
     *              their wrapper classes, {@code String}, {@code BigInteger}, {@code BigDecimal},
     *              or other custom types handled by an {@code ObjectParser}.
     * @param value The string value to be parsed. This value is expected to be suitable for conversion
     *              to the requested type, depending on its format and constraints of the type.
     * @return The parsed object, which is an instance of the specified type. If the type is
     *         unsupported or the value cannot be parsed,*/
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
        } else if (type == BigInteger.class) {
            return new BigInteger(value);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value);
        } else {
            final Object result = ObjectParser.parseObject(value);
            if (result != null) {
                return result;
            } else {
                throw new UnSupportedTypeException("unsupported type: " + type);
            }
        }
    }

    /**
     * Parses the given string value into an enumeration constant of the specified type.
     *
     * This method attempts to match the provided string value (case-insensitively) with
     * the name of one of the constants defined in the specified enumeration type. If a match
     * is found, the corresponding constant is returned. If no match is found, an
     * {@code InvalidEnumerationValueException} is thrown.
     *
     * @param <T>       The type of the enumeration.
     * @param enumType  The {@code Class} object of the enumeration type to parse. Must not be null.
     * @param value     The string value to be parsed into an enumeration constant.
     *                  Can be case-insensitively matched against the enumeration constant names.
     * @return The matched enumeration constant of the specified type.
     * @throws InvalidEnumerationValueException If the provided string value does not match any
     *                                          constant in the specified enumeration type.
     */
    public static <T extends Enum<?>> @NotNull T parseEnum(@NotNull Class<T> enumType, String value) {
        for (T constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) return constant;
        }
        throw new InvalidEnumerationValueException("Invalid enumeration value: "+value+" for enumeration "+enumType.getName());
    }
}
