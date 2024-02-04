package com.gmail.litalways.toolset.gui;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.StrUtil;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IceRain
 * @since 2022/01/20
 */
public class MainFormConvertCommonFunction {

    private final ToolWindowConvert component;
    private final AtomicBoolean flagCanEncode = new AtomicBoolean(true);
    private final AtomicBoolean flagCanDecode = new AtomicBoolean(true);
    private final AtomicBoolean lastCommandIsEncode = new AtomicBoolean(true);

    public MainFormConvertCommonFunction(ToolWindowConvert component) {
        this.component = component;
        this.component.buttonConvertCommonEncode.addActionListener(e -> {
            flagCanDecode.set(false);
            this.encode();
            flagCanDecode.set(true);
        });
        this.component.buttonConvertCommonDecode.addActionListener(e -> {
            flagCanEncode.set(false);
            this.decode();
            flagCanEncode.set(true);
        });
        this.component.buttonConvertCommonClean.addActionListener(e -> this.clean());
        this.component.textareaConvertCommonDecoded.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                flagCanDecode.set(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                flagCanDecode.set(true);
            }
        });
        this.component.textareaConvertCommonDecoded.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (flagCanEncode.get()) {
                    autoEncode();
                    lastCommandIsEncode.set(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (flagCanEncode.get()) {
                    autoEncode();
                    lastCommandIsEncode.set(true);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.component.textareaConvertCommonEncoded.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                flagCanEncode.set(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                flagCanEncode.set(true);
            }
        });
        this.component.textareaConvertCommonEncoded.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (flagCanDecode.get()) {
                    autoDecode();
                    lastCommandIsEncode.set(false);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (flagCanDecode.get()) {
                    autoDecode();
                    lastCommandIsEncode.set(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.component.selectConvertCommonCharset.addItemListener(e -> redo());
        this.component.radioConvertCommonBase64.addActionListener(this::radioChanged);
        this.component.radioConvertCommonHex.addActionListener(this::radioChanged);
        this.component.radioConvertCommonHtml.addActionListener(this::radioChanged);
        this.component.radioConvertCommonUnicode.addActionListener(this::radioChanged);
        this.component.radioConvertCommonUriComponent.addActionListener(this::radioChanged);
        this.component.radioConvertCommonJson.addActionListener(this::radioChanged);
        this.component.radioConvertCommonJsonToXml.addActionListener(this::radioChanged);
        this.component.radioConvertCommonTime.addActionListener(this::radioChanged);
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.component.scrollConvertCommonDecoded, this.component.scrollConvertCommonEncoded);
        this.component.scrollConvertCommonDecoded.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollConvertCommonDecoded.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollConvertCommonEncoded.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollConvertCommonEncoded.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void radioChanged(ActionEvent e) {
        if (((JRadioButton) e.getSource()).isSelected()) {
            redo();
        }
    }

    private void redo() {
        if (this.component.checkConvertCommonAuto.isSelected()) {
            if (this.lastCommandIsEncode.get()) {
                flagCanDecode.set(false);
                encode();
            } else {
                flagCanEncode.set(false);
                decode();
            }
        }
    }

    private Charset getCharset() throws UnsupportedEncodingException {
        int encodingModelIndex = this.component.selectConvertCommonCharset.getSelectedIndex();
        Object selectedObjects = this.component.selectConvertCommonCharset.getModel().getSelectedItem();
        if (encodingModelIndex == 0) {
            return Charset.defaultCharset();
        } else {
            return Charset.forName((String) selectedObjects);
        }
    }

    private void autoEncode() {
        if (this.component.checkConvertCommonAuto.isSelected()) {
            encode();
        }
    }

    private void clean() {
        flagCanEncode.set(false);
        flagCanDecode.set(false);
        this.component.textareaConvertCommonEncoded.setText("");
        this.component.textareaConvertCommonDecoded.setText("");
        flagCanEncode.set(true);
        flagCanDecode.set(true);
    }

    private void encode() {
        String decoded = this.component.textareaConvertCommonDecoded.getText();
        if (decoded == null || decoded.trim().isEmpty()) {
            return;
        }
        if (this.component.checkConvertCommonLine.isSelected()) {
            String[] split = decoded.replace("\r", "").split("\n");
            StringBuilder sb = new StringBuilder();
            for (String eachLine : split) {
                String encoded;
                try {
                    encoded = encode(eachLine);
                } catch (Exception e) {
                    encoded = MessageUtil.getMessage("convert.common.encode.tip.fail", e.getClass().getName(), e.getLocalizedMessage());
                }
                sb.append(encoded).append("\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            this.component.textareaConvertCommonEncoded.setText(result);
        } else {
            String encoded;
            try {
                encoded = encode(decoded);
            } catch (Exception e) {
                encoded = MessageUtil.getMessage("convert.common.encode.tip.fail", e.getClass().getName(), e.getLocalizedMessage());
            }
            this.component.textareaConvertCommonEncoded.setText(encoded);
        }
    }

    private String encode(String decoded) throws UnsupportedEncodingException, ParseException {
        if (decoded == null || decoded.trim().isEmpty()) {
            return "";
        }
        if (this.component.radioConvertCommonBase64.isSelected()) {
            return new String(Base64.getEncoder().encode(decoded.getBytes(getCharset())), getCharset());
        } else if (this.component.radioConvertCommonHex.isSelected()) {
            if (decoded.startsWith("0x")) {
                // HEX 从十六进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 16);
                return MessageUtil.getMessage("convert.common.encode.tip.hex", l, Long.toOctalString(l), Long.toBinaryString(l));
            } else if (decoded.startsWith("0o")) {
                // OCT 从八进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 8);
                return MessageUtil.getMessage("convert.common.encode.tip.oct", l, Long.toHexString(l), Long.toBinaryString(l));
            } else if (decoded.startsWith("0b")) {
                // BIN 从二进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 2);
                return MessageUtil.getMessage("convert.common.encode.tip.bin", l, Long.toHexString(l), Long.toOctalString(l));
            } else if (decoded.startsWith("0d")) {
                // DEC 从十进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 10);
                return MessageUtil.getMessage("convert.common.encode.tip.dec", Long.toHexString(l), Long.toOctalString(l), Long.toBinaryString(l));
            } else {
                // 文本转换十六进制
                byte[] decodedBytes = decoded.getBytes(getCharset());
                return StrUtil.bytesToHex(decodedBytes, getCharset());
            }
        } else if (this.component.radioConvertCommonHtml.isSelected()) {
            return StringEscapeUtils.escapeHtml4(decoded);
        } else if (this.component.radioConvertCommonUnicode.isSelected()) {
            return StringEscapeUtils.escapeJava(decoded);
        } else if (this.component.radioConvertCommonUriComponent.isSelected()) {
            return URLEncoder.encode(decoded, getCharset())
                    .replace("+", "%20").replace("%21", "!")
                    .replace("%28", "(").replace("%29", ")")
                    .replace("%7E", "~").replace("%27", "'");
        } else if (this.component.radioConvertCommonJson.isSelected()) {
            return StringEscapeUtils.escapeJson(decoded);
        } else if (this.component.radioConvertCommonJsonToXml.isSelected()) {
            JSON json = JSONUtil.parse(decoded);
            String xml = JSONUtil.toXmlStr(json);
            // 默认的是平铺格式
            if (!this.component.checkConvertCommonLine.isSelected()) {
                try {
                    xml = XmlUtil.toStr(XmlUtil.parseXml(xml));
                    xml = xml.replace("\r\n", "\n");
                } catch (Exception ignored) {}
            }
            return xml;
        } else if (this.component.radioConvertCommonTime.isSelected()) {
            Long unixTimestamp = null;
            try {
                unixTimestamp = Long.parseLong(decoded);
            } catch (NumberFormatException ignored) {
            }
            if (unixTimestamp != null) {
                Calendar instance = Calendar.getInstance(Locale.getDefault());
                instance.setTimeInMillis(unixTimestamp);
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").format(instance.getTime());
            } else {
                String[] s = decoded.split(" ");
                if (s.length > 2) {
                    TimeZone timeZone = null;
                    try {
                        timeZone = TimeZone.getTimeZone(s[s.length - 1]);
                    } catch (Exception ignored) {
                    }
                    if (timeZone != null) {
                        // 去空格和末尾的时区ID
                        decoded = decoded.substring(0, decoded.length() - s[s.length - 1].length() - 1);
                        DateTime dateTime = DateUtil.parse(decoded);
                        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").parse(dateTime.toString("yyyy-MM-dd HH:mm:ss.SSS") + " " + timeZone.getID());
                        return MessageUtil.getMessage("convert.common.encode.tip.time", parse.getTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").format(parse));
                    }
                }
                DateTime dateTime = DateUtil.parse(decoded);
                return MessageUtil.getMessage("convert.common.encode.tip.time", dateTime.getTime(), dateTime.toString("yyyy-MM-dd HH:mm:ss.SSS z"));
            }
        }
        throw new IllegalArgumentException(MessageUtil.getMessage("convert.common.encode.tip.not.select.type"));
    }

    private void autoDecode() {
        if (this.component.checkConvertCommonAuto.isSelected()) {
            decode();
        }
    }

    private void decode() {
        String encoded = this.component.textareaConvertCommonEncoded.getText();
        if (encoded == null || encoded.trim().isEmpty()) {
            return;
        }
        if (this.component.checkConvertCommonLine.isSelected()) {
            String[] split = encoded.replace("\r", "").split("\n");
            StringBuilder sb = new StringBuilder();
            for (String eachLine : split) {
                String decoded;
                try {
                    decoded = decode(eachLine);
                } catch (Exception e) {
                    decoded = MessageUtil.getMessage("convert.common.decode.tip.fail", e.getClass().getName(), e.getLocalizedMessage());
                }
                sb.append(decoded).append("\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            if (split.length == 1 && result.split("\n").length > 1) {
                this.component.checkConvertCommonLine.setSelected(false);
            }
            this.component.textareaConvertCommonDecoded.setText(result);
        } else {
            String decoded;
            try {
                decoded = decode(encoded);
            } catch (Exception e) {
                decoded = MessageUtil.getMessage("convert.common.decode.tip.fail", e.getClass().getName(), e.getLocalizedMessage());
            }
            this.component.textareaConvertCommonDecoded.setText(decoded);
        }
    }

    private String decode(String encoded) throws UnsupportedEncodingException {
        if (encoded == null || encoded.trim().isEmpty()) {
            return "";
        }
        if (this.component.radioConvertCommonBase64.isSelected()) {
            return new String(Base64.getDecoder().decode(encoded.getBytes(getCharset())), getCharset());
        } else if (this.component.radioConvertCommonHex.isSelected()) {
            return new String(HexUtil.decodeHex(encoded), getCharset());
        } else if (this.component.radioConvertCommonHtml.isSelected()) {
            return StringEscapeUtils.unescapeHtml4(encoded);
        } else if (this.component.radioConvertCommonUnicode.isSelected()) {
            return StringEscapeUtils.unescapeJava(encoded);
        } else if (this.component.radioConvertCommonUriComponent.isSelected()) {
            return URLDecoder.decode(encoded
                            .replace("%20", "+").replace("!", "%21")
                            .replace("(", "%28").replace(")", "%29")
                            .replace("~", "%7E").replace("'", "%27")
                    , getCharset());
        } else if (this.component.radioConvertCommonJson.isSelected()) {
            return StringEscapeUtils.unescapeJson(encoded);
        } else if (this.component.radioConvertCommonJsonToXml.isSelected()) {
            JSONObject json = JSONUtil.parseFromXml(encoded);
            String jsonString = json.toJSONString(4);
            // 默认的是缩进格式
            if (this.component.checkConvertCommonLine.isSelected()) {
                jsonString = MainFormFormatFunction.unFormatJson(jsonString);
                jsonString = jsonString.replace("\r\n", "\n");
            }
            return jsonString;
        } else if (this.component.radioConvertCommonTime.isSelected()) {
            throw new IllegalArgumentException(MessageUtil.getMessage("convert.common.decode.tip.time.not.support"));
        }
        throw new IllegalArgumentException(MessageUtil.getMessage("convert.common.decode.tip.not.select.type"));
    }

}
