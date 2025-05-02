package com.habbashx.manager;

import com.habbashx.property.PropertiesStore;
import com.habbashx.property.PropertyElement;
import com.habbashx.property.PropertyValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Properties;

public class PropertiesManager {

    private final PropertiesStore propertiesStore;
    private final File file;
    private final Properties properties = new Properties();

    public PropertiesManager(PropertiesStore propertiesStore , File file) {
        this.propertiesStore = propertiesStore;
        this.file = file;
        loadProperties();
    }

    public void store() {
        for (PropertyElement element : propertiesStore.getPropertyElements().values()) {
            properties.setProperty(element.getKey(),element.getPropertyValue().getRawValue());
        }
        storeProperties();
    }

    public void storeProperties() {
        storeProperties("");
    }

    public void storeProperties(String comments) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream,comments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadProperties() {
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);

            propertiesStore.getPropertyElements().clear();

            for (String key : properties.stringPropertyNames()) {
                String rawValue = properties.getProperty(key);
                PropertyValue value = new PropertyValue(rawValue);
                PropertyElement propertyElement = new PropertyElement(key,value);
                propertiesStore.addProperty(key,value);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public PropertiesStore getPropertiesStore() {
        return propertiesStore;
    }
}
