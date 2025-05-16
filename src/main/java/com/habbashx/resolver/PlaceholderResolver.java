package com.habbashx.resolver;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderResolver extends Resolver {

    /**
     * A {@code Pattern} object defining the regular expression used for matching placeholders
     * in the format "${key}". This pattern captures the content inside the curly braces as
     * a group, which can be extracted for further processing or replacement.
     *
     * The regular expression:
     * - Matches strings that start with "${" and end with "}".
     * - Captures the content within the braces (i.e., the key) using a non-greedy match.
     *
     * This pattern is primarily used in the {@link PlaceholderResolver} class to identify
     * and process placeholders within input strings, replacing them with corresponding values
     * from provided sources such as properties, system properties, or environment variables.
     */
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.+?)}");

    /**
     * Resolves the given string value by replacing placeholders in the format `${key}`
     * with their corresponding values from the provided {@code Properties} object.
     * The method uses a specific pattern to identify placeholders and fetches the
     * replacement value from the following sources in order:
     *
     * 1. The provided {@code Properties} object.
     * 2. System properties.
     * 3. Environment variables.
     *
     * If no replacement value is found, the placeholder remains unchanged.
     *
     * @param value      The input string potentially containing placeholders to be resolved.
     *                   Must not be null.
     * @param properties A {@code Properties} object containing key-value pairs used for
     *                   resolving the placeholders. Must not be null.
     * @return A resolved string with all placeholders replaced with their corresponding
     *         values. If no placeholders are found, or if the input value is empty,
     *         the method returns the original value.
     */
    @Override
    public @NotNull String resolve(@NotNull String value , Properties properties) {

        if (value.isEmpty()) return value;

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
