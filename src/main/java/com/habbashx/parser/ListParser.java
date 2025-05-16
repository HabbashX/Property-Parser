package com.habbashx.parser;

import com.habbashx.exception.EmptyItemException;
import com.habbashx.exception.EmptyListException;
import org.intellij.lang.annotations.Language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code ListParser} class provides utility methods for parsing a comma-separated
 * string into an unmodifiable list of objects. The type of objects in the parsed list
 * is determined by the provided parameterized type.
 *
 * This class is final, meaning it cannot be subclassed. It is designed to be used in
 * scenarios where a string representation of a list must be converted into a list with a
 * specific type of elements.
 *
 * Exceptions:
 * - Throws {@code EmptyItemException} if any item in the raw string is empty after trimming.
 * - Throws {@code EmptyListException} if the input raw string results in an empty list.
 * - Throws {@code UnsupportedOperationException} if the parameterized type is not supported.
 */
public final class ListParser {

    /**
     * Parses a comma-separated string into an unmodifiable list of objects based on the specified type.
     * The method supports parsing to specific types, such as {@code String} and {@code Integer}.
     *
     * @param rawValue the raw string containing comma-separated values, must not be null
     * @param parameterizedType the type of elements expected in the list; supported types are {@code String} and {@code Integer}
     * @return an unmodifiable list of parsed objects based on the specified parameterized type
     * @throws EmptyItemException if any item in the string is empty or blank
     * @throws EmptyListException if the input string does not contain any items
     * @throws UnsupportedOperationException if the provided parameterized type is not supported
     */
    public static @Unmodifiable List<Object> parseList(@NotNull String rawValue, Type parameterizedType) {

        @Language("RegExp")
        String[] items = rawValue.split(",");

        if (items.length > 0) {

            return Arrays.stream(items).map(item -> {
                String trimmedItem = item.trim();

                if (!trimmedItem.isEmpty() || !trimmedItem.isBlank()) {
                    if (parameterizedType == String.class) {
                        return trimmedItem;
                    } else if (parameterizedType == Integer.class) {
                        return Integer.parseInt(trimmedItem);
                    } else {
                        throw new UnsupportedOperationException("unsupported parameterized type: " + parameterizedType);
                    }
                } else {

                    throw new EmptyItemException("item is empty");
                }
            }).collect(Collectors.toUnmodifiableList());

        } else {
            throw new EmptyListException("list is empty");
        }
    }
}
