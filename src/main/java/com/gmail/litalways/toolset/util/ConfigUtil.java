package com.gmail.litalways.toolset.util;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 简单配置封装工具类
 *
 * @author IceRain
 * @since 2023/04/06
 */
public class ConfigUtil {

    private static final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();

    public static boolean isValueSet(String key) {
        return propertiesComponent.isValueSet(key);
    }

    public static String getString(String key, @NotNull String defaultValue) {
        return propertiesComponent.getValue(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return propertiesComponent.getBoolean(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return propertiesComponent.getFloat(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return propertiesComponent.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return propertiesComponent.getLong(key, defaultValue);
    }

    public static List<String> getList(String key, @NotNull List<String> defaultValue) {
        if (propertiesComponent.isValueSet(key)) {
            return propertiesComponent.getList(key);
        } else {
            propertiesComponent.setList(key, defaultValue);
            return defaultValue;
        }
    }

    public static void setString(String key, @NotNull String value, @NotNull String defaultValue) {
        propertiesComponent.setValue(key, value, defaultValue);
    }

    public static void setBoolean(String key, boolean value, boolean defaultValue) {
        propertiesComponent.setValue(key, value, defaultValue);
    }

    public static void setFloat(String key, float value, float defaultValue) {
        propertiesComponent.setValue(key, value, defaultValue);
    }

    public static void setInt(String key, int value, int defaultValue) {
        propertiesComponent.setValue(key, value, defaultValue);
    }

    public static void setLong(String key, long value, long defaultValue) {
        propertiesComponent.setValue(key, value, defaultValue);
    }

    public static void setList(String key, @NotNull List<String> value) {
        propertiesComponent.setList(key, value);
    }

    public static void remove(String key) {
        propertiesComponent.unsetValue(key);
    }

}
