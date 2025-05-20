package com.habbashx.parser;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ObjectInputStream;

import java.io.FileInputStream;

/**
 * Provides utility functionality to parse objects from serialized files.
 *
 * The {@code ObjectParser} class is designed to deserialize an object from a file
 * containing its serialized representation. This utility is useful for scenarios
 * where objects need to be reconstructed from their persisted forms.
 */
public class ObjectParser {

    /**
     * Parses and deserializes an object from the given serialized file.
     *
     * This method attempts to read and deserialize an object from the specified file path,
     * which is expected to contain a serialized object. If the operation fails due to any
     * exception (e.g., file not found, input/output error, or deserialization error), the
     * method returns {@code null}.
     *
     * @param objectFile The file path to the serialized object file. This file must contain
     *                   a valid serialized object.
     * @return The deserialized object if the operation is successful, or {@code null} if
     *         an error occurs during deserialization.
     */
    public static @Unmodifiable @Nullable Object parseObject(String objectFile) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));
            return ois.readObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}
