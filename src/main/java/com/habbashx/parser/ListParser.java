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

public final class ListParser {

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
