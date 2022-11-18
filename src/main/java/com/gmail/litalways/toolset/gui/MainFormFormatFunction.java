package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gmail.litalways.toolset.enums.KeyEnum;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import org.w3c.dom.Document;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormFormatFunction {

    private final MainForm mainForm;

    public MainFormFormatFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonFormatDo.addActionListener(this::format);
        this.mainForm.buttonFormatUndo.addActionListener(this::unFormat);
    }

    private void format(ActionEvent e) {
        String text = this.mainForm.textareaFormat.getText();
        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
        if (tmp.length() < 1) {
            return;
        }
        if ("{".equals(tmp.substring(0, 1))) {
            JSONObject jsonObject = JSONUtil.parseObj(text);
            text = jsonObject.toJSONString(4);
            this.mainForm.textareaFormat.setText(text);
        } else if ("[".equals(tmp.substring(0, 1))) {
            JSONArray jsonArray = JSONUtil.parseArray(text);
            text = jsonArray.toJSONString(4);
            this.mainForm.textareaFormat.setText(text);
        } else if ("<".equals(tmp.substring(0, 1))) {
            Document document = XmlUtil.parseXml(text);
            text = XmlUtil.toStr(document, true);
            this.mainForm.textareaFormat.setText(text);
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Only support JSON and XML now!", null, null, NotificationType.ERROR)
                    .notify(null);
        }
    }

    private static String unFormatJson(String formattedJson) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean needIgnoreQuote = false;
        boolean needUnFormat = true;
        for (char c : formattedJson.toCharArray()) {
            switch (c) {
                // removed char
                case '\r':
                case '\n':
                case '\t':
                case ' ':
                    if (!needUnFormat) {
                        stringBuilder.append(c);
                    }
                    needIgnoreQuote = false;
                    break;
                // ignore quote
                case '\\':
                    stringBuilder.append(c);
                    needIgnoreQuote = true;
                    break;
                // in key-value
                case '\"':
                    stringBuilder.append(c);
                    if (!needIgnoreQuote) {
                        needUnFormat = !needUnFormat;
                    }
                    needIgnoreQuote = false;
                    break;
                // normal char
                default:
                    stringBuilder.append(c);
                    needIgnoreQuote = false;
                    break;
            }
        }
        return stringBuilder.toString();
    }

    private static String unFormatXml(String formattedXml) {
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
            if (s == null || s.trim().length() == 0) {
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder();
            boolean inTag = false;
            boolean inQuote = false;
            for (char c : s.toCharArray()) {
                switch (c) {
                    // remove char
                    case '\r':
                    case '\n':
                    case '\t':
                    case ' ':
                        if (inTag) {
                            stringBuilder.append(c);
                        }
                        break;
                    case '<':
                        stringBuilder.append(c);
                        if (!inQuote) {
                            inTag = true;
                        }
                        break;
                    case '>':
                        stringBuilder.append(c);
                        if (!inQuote) {
                            inTag = false;
                        }
                        break;
                    case '\"':
                        stringBuilder.append(c);
                        if (inTag) {
                            inQuote = !inQuote;
                        }
                        break;
                    // normal char
                    default:
                        stringBuilder.append(c);
                        break;
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

    private void unFormat(ActionEvent e) {
        String text = this.mainForm.textareaFormat.getText();
        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
        if (tmp.length() < 1) {
            return;
        }
        if ("{".equals(tmp.substring(0, 1))) {
            JSONObject jsonObject = JSONUtil.parseObj(text);
            text = jsonObject.toJSONString(4);
            text = unFormatJson(text);
            this.mainForm.textareaFormat.setText(text);
        } else if ("[".equals(tmp.substring(0, 1))) {
            JSONArray jsonArray = JSONUtil.parseArray(text);
            text = jsonArray.toJSONString(4);
            text = unFormatJson(text);
            this.mainForm.textareaFormat.setText(text);
        } else if ("<".equals(tmp.substring(0, 1))) {
            Document document = XmlUtil.parseXml(text);
            text = XmlUtil.toStr(document, true);
            text = unFormatXml(text);
            this.mainForm.textareaFormat.setText(text);
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Only support JSON and XML now!", null, null, NotificationType.ERROR)
                    .notify(null);
        }
    }

}
