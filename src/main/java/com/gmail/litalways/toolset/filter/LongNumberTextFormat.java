package com.gmail.litalways.toolset.filter;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * @author IceRain
 * @since 2022/11/24
 */
public class LongNumberTextFormat extends Format {

    private NumberFormat numberFormat;

    public static LongNumberTextFormat getInstance() {
        LongNumberTextFormat textFormat = new LongNumberTextFormat();
        textFormat.numberFormat = NumberFormat.getNumberInstance();
        return textFormat;
    }

    @Override
    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        if (obj == null || String.valueOf(obj).isEmpty()) {
            return null;
        }
        String objStr = String.valueOf(obj);
        objStr = objStr.replaceAll("\\D", "");
        return new StringBuffer(objStr);
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return numberFormat.parseObject(source.replaceAll("\\D", ""), pos);
    }

}
