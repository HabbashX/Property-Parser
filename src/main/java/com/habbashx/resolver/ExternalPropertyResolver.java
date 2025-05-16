package com.habbashx.resolver;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class is a concrete implementation of the {@link Resolver} abstract class that resolves
 * external property values from a specified properties file.
 *
 * ExternalPropertyResolver is responsible for fetching the values of properties stored
 * in external `.properties` files. The class works by parsing a specially formatted input
 * string, identifying the file name and the required property key, loading the file if
 * it has not already been loaded, and retrieving the property value.
 *
 * The expected input format for the resolve method is:
 *  `>{fileName}:propertyName`
 * where:
 * - `fileName` specifies the name of the properties file (appending `.properties` if not specified).
 * - `propertyName` specifies the key whose value needs to be resolved.
 *
 * If the file name does not contain the `.properties` extension, it will automatically
 * append `.properties` to the file name.
 *
 * Resolving a property involves:
 * - Parsing the input to extract the file name and property key.
 * - Loading the properties file (if not already loaded).
 * - Returning the value associated with the property key from the file.
 *
 * The resolution process assumes that the input follows the specified format;
 * invalid input may result in unexpected behavior. Any IO exceptions encountered
 * during file loading will result in a {@link RuntimeException}.
 *
 * Thread Safety:
 * - This implementation is not thread-safe as it uses a single {@link Properties}
 *   instance to cache properties. Concurrent modifications or access may lead
 *   to inconsistent behavior.
 *
 * Error Handling:
 * - If the specified file cannot be found or loaded, a {@link RuntimeException}
 *   will be raised.
 * - If the specified property key does not exist in the file, this method will
 *   return {@code null}.
 */
public final class ExternalPropertyResolver extends Resolver {

    private final Properties externalProperties = new Properties();

    /**
     * Resolves the given string value by extracting property values from an external
     * properties file based on a specific format. The input value is expected to follow
     * a certain pattern to enable precise parsing and resolution.
     *
     * The method works by loading the specified properties file, extracting a key,
     * and returning the associated value. If the value format does not meet the
     * resolution criteria, the method may return null.
     *
     * @param value      The input string to be resolved. Must not be null and needs to
     *                   follow a specific format for successful resolution.
     * @param properties A {@code Properties} object, for compatibility, though it's not
     *                   utilized directly in this method.
     * @return The resolved string value, or null if the resolution fails. May return
     *         the input value unchanged if it is empty.
     */
    @Override
    public @Nullable String resolve(@NotNull String value, Properties properties) {

        if (value.isEmpty()) {
            return value;
        }

        @Language("RegExp")
        String []parts = value.split(":");

        if (parts.length <= 2) {
            String fileName = parts[0];
            int lastIndex = fileName.indexOf(">");
            String extractedFileName = fileName.substring(1,lastIndex);
            String property = parts[1];

            if (!extractedFileName.contains(".properties")) {
                extractedFileName = extractedFileName + ".properties";
            }
            loadProperties(extractedFileName);
            return getPropertyValue(property);
        }
        return null;
    }

    private String getPropertyValue(String property) {
        return externalProperties.getProperty(property);
    }

    private void loadProperties(String fileName) {

        try (InputStream inputStream = new FileInputStream(fileName)) {
            externalProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
