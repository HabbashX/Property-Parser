package com.habbashx.resolver.registry;

import com.habbashx.resolver.Resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Registry responsible for managing a chain of {@link Resolver} instances.
 *
 * <p>
 * Resolvers are used to process and transform raw property values before they are
 * injected into target fields. Common use cases include:
 * </p>
 *
 * <ul>
 *     <li>Placeholder replacement (e.g. ${key})</li>
 *     <li>Environment variable resolution</li>
 *     <li>External property resolution</li>
 *     <li>Custom preprocessing logic</li>
 * </ul>
 *
 * <p>
 * Resolvers are executed in registration order.
 * </p>
 */
public class ResolverRegistry {

    /**
     * Ordered list of registered resolvers.
     */
    private final List<Resolver> resolversRegistry = new ArrayList<>();

    /**
     * Registers a new resolver into the chain.
     *
     * @param resolver the resolver to add
     */
    public void register(Resolver resolver) {
        resolversRegistry.add(resolver);
    }

    /**
     * Resolves a value by applying all registered resolvers sequentially.
     *
     * <p>
     * Each resolver receives the output of the previous one, allowing chained
     * transformation of the original value.
     * </p>
     *
     * @param value the raw input value
     * @param properties the properties context used by resolvers
     * @return the fully resolved value after all transformations
     */
    public String resolve(String value, Properties properties) {

        String result = value;

        for (Resolver resolver : resolversRegistry) {
            result = resolver.resolve(result, properties);
        }

        return result;
    }
}