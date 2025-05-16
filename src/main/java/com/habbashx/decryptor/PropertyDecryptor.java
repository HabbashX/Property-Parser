package com.habbashx.decryptor;

/**
 * An interface for implementing custom decryption logic for encrypted properties.
 *
 * Classes implementing this interface should provide the logic to decrypt
 * a given encrypted value into its original form.
 */
public interface PropertyDecryptor {
    /**
     * Decrypts the given encrypted value and returns the original, decrypted string.
     *
     * This method is designed to handle the decryption of a string value that has
     * been previously encrypted. The specific decryption logic is provided by the
     * implementing class of the {@link PropertyDecryptor} interface.
     *
     * @param encryptedValue The encrypted string value to be decrypted. Can be null
     *                       if no encrypted value is provided.
     * @return The decrypted string value. If the input is null, the return value may
     *         also be null depending on the implementation.
     */
    String decrypt(String encryptedValue);
}
