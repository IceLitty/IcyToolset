package com.gmail.litalways.toolset.filter;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * @author IceRain
 * @since 2022/11/24
 */
public class FileSizeTextFormat extends Format {

    private NumberFormat numberFormat;

    public static FileSizeTextFormat getInstance() {
        FileSizeTextFormat fileSizeTextFormat = new FileSizeTextFormat();
        fileSizeTextFormat.numberFormat = NumberFormat.getNumberInstance();
        return fileSizeTextFormat;
    }

    @Override
    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        if (obj == null || String.valueOf(obj).length() == 0) {
            return null;
        }
        String objStr = String.valueOf(obj);
        char lastChar = objStr.charAt(objStr.length() - 1);
        switch (lastChar) {
            case 'g':
            case 'G':
            case 'm':
            case 'M':
            case 'k':
            case 'K':
                try {
                    String substring = objStr.substring(0, objStr.length() - 1);
                    BigDecimal bigDecimal = new BigDecimal(substring);
                    toAppendTo = numberFormat.format(bigDecimal, toAppendTo, pos);
                    toAppendTo.append(lastChar);
                    return toAppendTo;
                } catch (Exception ignored) {
                    return null;
                }
            default:
                try {
                    return numberFormat.format(obj, toAppendTo, pos);
                } catch (Exception ignored) {
                    return null;
                }
        }
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        if (source == null || source.length() == 0) {
            return null;
        }
        char lastChar = source.charAt(source.length() - 1);
        switch (lastChar) {
            case 'g':
            case 'G':
            case 'm':
            case 'M':
            case 'k':
            case 'K':
                String substring = source.substring(0, source.length() - 1);
                Object o = numberFormat.parseObject(substring, pos);
                if (o == null) {
                    return null;
                }
                return new BigDecimal(String.valueOf(o)).toPlainString() + String.valueOf(lastChar).toLowerCase();
            default:
                return numberFormat.parseObject(source, pos);
        }
    }

}
