package com.habbashx.resolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderResolver extends Resolver {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    @Override
    public @NotNull String resolve(@NotNull String value , Properties properties) {

        if (value.isEmpty()) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PATTERN.matcher(value);

        int lastEnd = 0;
        while (matcher.find()) {
            result.append(value, lastEnd, matcher.start());

            String key = matcher.group(1);

            String replacement = getReplacement(key, properties);

            result.append(replacement);
            lastEnd = matcher.end();
        }

        result.append(value, lastEnd, value.length());

        return result.toString();
    }

    private static @NotNull String getReplacement(String key, @NotNull Properties properties) {
        String replacement = properties.getProperty(key);

        if (replacement != null) {
            return replacement;
        }

        replacement = System.getProperty(key);

        if (replacement != null) {
            return replacement;
        }

        replacement = System.getenv(key);

        if (replacement != null) {
            return replacement;
        }

        return "${" + key + "}";
    }
}
