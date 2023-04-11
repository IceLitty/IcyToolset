package com.gmail.litalways.toolset.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @author IceRain
 * @since 2022/01/21
 */
public class StrUtil {

//    private static final Pattern showMaxPattern = Pattern.compile("^(.|\\s)+\\.\\.\\.<and \\d+ chars>$");
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String showMax(String src, int length) {
        if (src == null) {
            return "";
        }
        if (src.length() > length) {
            return src.substring(0, length) + MessageUtil.getMessage("util.str.char.limit.tip", src.length() - length);
        }
        return src;
    }

    public static boolean endsWithShowMax(String src) {
        // Not use regex because it will cause StackOverflowException
        String[] s = MessageUtil.getBundleString("util.str.char.limit.tip").split("\\{0}");
        return src.endsWith(s[s.length == 0 ? 0 : s.length - 1]);
    }

    // Byte String to Hex solution from https://stackoverflow.com/a/9855338
    public static String bytesToHex(byte[] bytes, String charset) throws UnsupportedEncodingException {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, charset);
    }

}
