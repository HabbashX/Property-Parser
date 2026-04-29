package com.habbashx.decryptor.registry;

import com.habbashx.decryptor.PropertyDecryptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry responsible for managing {@link PropertyDecryptor} instances.
 * <p>
 * This class provides a centralized mechanism to register, cache, and retrieve
 * decryptors used for processing encrypted configuration values.
 * </p>
 *
 * <p>
 * If a decryptor is not explicitly registered, it will be automatically instantiated
 * using its no-argument constructor and cached for future use.
 * </p>
 *
 * <p>
 * Thread-safety: Uses {@link ConcurrentHashMap} to ensure safe concurrent access
 * in multi-threaded environments.
 * </p>
 */
public class PropertyDecryptorRegistry {

    /**
     * Internal registry mapping decryptor types to their singleton-like instances.
     */
    private final Map<Class<? extends PropertyDecryptor>, PropertyDecryptor> registry =
            new ConcurrentHashMap<>();

    /**
     * Registers a custom decryptor instance for a specific decryptor type.
     *
     * @param type the decryptor class type
     * @param decryptor the decryptor instance to associate with the type
     */
    public void register(Class<? extends PropertyDecryptor> type,
                         PropertyDecryptor decryptor) {
        registry.put(type, decryptor);
    }

    /**
     * Decrypts the given encrypted value using the specified decryptor type.
     *
     * <p>
     * If the decryptor is not already registered, it will be lazily instantiated
     * using its no-argument constructor and cached.
     * </p>
     *
     * @param type the decryptor class to use for decryption
     * @param value the encrypted value to decrypt
     * @return the decrypted value
     * @throws RuntimeException if the decryptor cannot be instantiated
     */
    public String decrypt(Class<? extends PropertyDecryptor> type,
                          String value) {

        PropertyDecryptor decryptor = registry.get(type);

        if (decryptor == null) {
            try {
                decryptor = type.getDeclaredConstructor().newInstance();
                registry.put(type, decryptor);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create decryptor: " + type.getName(), e);
            }
        }

        return decryptor.decrypt(value);
    }
}