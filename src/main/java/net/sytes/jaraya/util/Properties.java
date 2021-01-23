package net.sytes.jaraya.util;

import lombok.Builder;
import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.UtilException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Builder
public class Properties {

    private static final String DEFAULT_FILEPATH = "local.properties";

    private final String filepath;

    public static String get(String key) throws UtilException {
        return Properties.builder().build().getProperty(key);
    }

    public static String get(String key, String filepath) throws UtilException {
        return Properties.builder().filepath(filepath).build().getProperty(key);
    }

    public String getProperty(String key) throws UtilException {
        return getProperties().get(key);
    }

    public static Map<String, String> gets() throws UtilException {
        return Properties.builder().build().getProperties();
    }

    public static Map<String, String> gets(String filepath) throws UtilException {
        return Properties.builder().filepath(filepath).build().getProperties();
    }

    private Map<String, String> getProperties() throws UtilException {
        String path = filepath != null && !filepath.isEmpty() ? filepath : DEFAULT_FILEPATH;
        try (InputStream is = new FileInputStream(path)) {
            return propertiesMap(is);
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

    private Map<String, String> propertiesMap(InputStream is) throws UtilException {
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            java.util.Properties properties = new java.util.Properties();
            properties.load(isr);
            Map<String, String> list = new HashMap<>();
            for (Property prop : Property.values()) {
                list.put(prop.name(), properties.getProperty(prop.name()));
            }
            return list;
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }
}
