package com.habbashx.resolver;

import java.util.Properties;

/**
 * The Resolver class is a sealed abstract class designed to provide a mechanism
 * for resolving string values based on certain rules or sources.
 * Subclasses are required to implement the resolve method, which defines how
 * the resolution process should be applied.
 *
 * The class is restricted in terms of permitted subclasses to ensure controlled
 * extension and consistent behavior.
 *
 * Permitted subclasses include:
 * - ExternalPropertyResolver
 * - PlaceholderResolver
 */
public sealed abstract class Resolver permits ExternalPropertyResolver, PlaceholderResolver {
    /**
     * Resolves the given string value using the provided properties. The resolution
     * process is defined by the implementation of this abstract method in the
     * subclasses. Typically, resolution involves replacing placeholders or processing
     * the input string based on certain rules or configurations.
     *
     * @param value      The input string to be resolved. Must not be null.
     * @param properties A {@code Properties} object containing key-value pairs used
     *                   for the resolution process. Must not be null.
     * @return A resolved string, potentially transformed based on the resolution
     *         logic. May return {@code null} if the implementation permits.
     */
    public abstract String resolve(String value , Properties properties);
}
