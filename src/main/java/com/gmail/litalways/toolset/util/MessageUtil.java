package com.gmail.litalways.toolset.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author IceRain
 * @since 2023/04/04
 */
public class MessageUtil {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("messages/applicationMessage");

    /**
     * 获取提示文本
     *
     * @param bundleKey key
     * @return 原生提示文本
     */
    public static String getBundleString(String bundleKey) {
        return messageBundle.getString(bundleKey);
    }

    /**
     * 获取格式化后的提示文本
     *
     * @param bundleKey key
     * @param params    文本参数
     * @return 提示文本
     */
    public static String getMessage(String bundleKey, Object... params) {
        return MessageFormat.format(messageBundle.getString(bundleKey), params);
    }

}
