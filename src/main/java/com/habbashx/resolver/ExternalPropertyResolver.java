package com.habbashx.resolver;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ExternalPropertyResolver extends Resolver {

    private final Properties externalProperties = new Properties();

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
