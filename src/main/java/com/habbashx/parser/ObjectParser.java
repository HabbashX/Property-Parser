package com.habbashx.parser;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ObjectInputStream;

import java.io.FileInputStream;

public class ObjectParser {

    public static @Unmodifiable @Nullable Object parseObject(String objectFile) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));
            return ois.readObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}
