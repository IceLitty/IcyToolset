package com.gmail.litalways.toolset.gui;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.application.ApplicationManager;
import org.w3c.dom.Document;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormFormatFunction {

    private final ToolWindowFormat component;

    public MainFormFormatFunction(ToolWindowFormat component) {
        this.component = component;
        this.component.buttonFormatDo.addActionListener(this::format);
        this.component.buttonFormatUndo.addActionListener(this::unFormat);
    }

    /**
     * 缩进格式化
     */
    private void format(ActionEvent e) {
        String text = this.component.editor.getDocument().getText();
        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
        if (tmp.isEmpty()) {
            return;
        }
        switch (tmp.substring(0, 1)) {
            case "{" -> {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(text);
                    text = jsonObject.toJSONString(4);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (JSONException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            case "[" -> {
                try {
                    JSONArray jsonArray = JSONUtil.parseArray(text);
                    text = jsonArray.toJSONString(4);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (JSONException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            case "<" -> {
                try {
                    Document document = XmlUtil.parseXml(text);
                    text = XmlUtil.toStr(document, true);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (UtilException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            default -> showErrorHint(MessageUtil.getMessage("format.tip.only.support.lang"));
        }
    }

    public static String unFormatJson(String formattedJson) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean needIgnoreQuote = false;
        boolean needUnFormat = true;
        for (char c : formattedJson.toCharArray()) {
            switch (c) {
                // removed char
                case '\r', '\n', '\t', ' ' -> {
                    if (!needUnFormat) {
                        stringBuilder.append(c);
                    }
                    needIgnoreQuote = false;
                }
                // ignore quote
                case '\\' -> {
                    stringBuilder.append(c);
                    needIgnoreQuote = true;
                }
                // in key-value
                case '\"' -> {
                    stringBuilder.append(c);
                    if (!needIgnoreQuote) {
                        needUnFormat = !needUnFormat;
                    }
                    needIgnoreQuote = false;
                }
                // normal char
                default -> {
                    stringBuilder.append(c);
                    needIgnoreQuote = false;
                }
            }
        }
        return stringBuilder.toString();
    }

    public static String unFormatXml(String formattedXml) {
        List<String> part1;
        String part2;
        if (formattedXml.contains("<![CDATA[") && formattedXml.contains("]]>")) {
            int start = formattedXml.indexOf("<![CDATA[");
            int end = formattedXml.lastIndexOf("]]>");
            part1 = new ArrayList<>();
            part1.add(formattedXml.substring(0, start));
            part1.add(formattedXml.substring(end + 3));
            part2 = formattedXml.substring(start, end + 3);
        } else {
            part1 = new ArrayList<>();
            part1.add(formattedXml);
            part2 = "";
        }
        for (int i = 0; i < part1.size(); i++) {
            String s = part1.get(i);
            if (s == null || s.trim().isEmpty()) {
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder();
            boolean inTag = false;
            boolean inQuote = false;
            for (char c : s.toCharArray()) {
                switch (c) {
                    // remove char
                    case '\r', '\n', '\t', ' ' -> {
                        if (inTag) {
                            stringBuilder.append(c);
                        }
                    }
                    case '<' -> {
                        stringBuilder.append(c);
                        if (!inQuote) {
                            inTag = true;
                        }
                    }
                    case '>' -> {
                        stringBuilder.append(c);
                        if (!inQuote) {
                            inTag = false;
                        }
                    }
                    case '\"' -> {
                        stringBuilder.append(c);
                        if (inTag) {
                            inQuote = !inQuote;
                        }
                    }
                    // normal char
                    default -> stringBuilder.append(c);
                }
            }
            part1.set(i, stringBuilder.toString());
        }
        if (part1.size() > 1) {
            return part1.get(0) + part2 + part1.get(1);
        } else {
            return part1.get(0);
        }
    }

    /**
     * 平铺格式化
     */
    public void unFormat(ActionEvent e) {
        String text = this.component.editor.getDocument().getText();
        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
        if (tmp.isEmpty()) {
            return;
        }
        switch (tmp.substring(0, 1)) {
            case "{" -> {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(text);
                    text = jsonObject.toJSONString(4);
                    text = unFormatJson(text);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (JSONException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            case "[" -> {
                try {
                    JSONArray jsonArray = JSONUtil.parseArray(text);
                    text = jsonArray.toJSONString(4);
                    text = unFormatJson(text);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (JSONException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            case "<" -> {
                try {
                    Document document = XmlUtil.parseXml(text);
                    text = XmlUtil.toStr(document, true);
                    text = unFormatXml(text);
                    text = text.replace("\r\n", "\n");
                    final String _text = text;
                    ApplicationManager.getApplication().runWriteAction(() -> this.component.editor.getDocument().setText(_text));
                } catch (UtilException e1) {
                    showErrorHint(MessageUtil.getMessage("format.tip.wrong.document", e1.getLocalizedMessage()));
                }
            }
            default -> showErrorHint(MessageUtil.getMessage("format.tip.only.support.lang"));
        }
    }

    void showErrorHint(String message) {
        HintManager.getInstance().showErrorHint(component.editor, message);
    }

}
