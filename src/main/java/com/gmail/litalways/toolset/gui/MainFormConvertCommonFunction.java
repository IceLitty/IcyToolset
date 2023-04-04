package com.gmail.litalways.toolset.gui;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IceRain
 * @since 2022/01/20
 */
public class MainFormConvertCommonFunction {

    private final MainForm mainForm;
    private final AtomicBoolean flagCanEncode = new AtomicBoolean(true);
    private final AtomicBoolean flagCanDecode = new AtomicBoolean(true);
    private final AtomicBoolean lastCommandIsEncode = new AtomicBoolean(true);

    public MainFormConvertCommonFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonConvertCommonEncode.addActionListener(e -> {
            flagCanDecode.set(false);
            this.encode();
            flagCanDecode.set(true);
        });
        this.mainForm.buttonConvertCommonDecode.addActionListener(e -> {
            flagCanEncode.set(false);
            this.decode();
            flagCanEncode.set(true);
        });
        this.mainForm.buttonConvertCommonClean.addActionListener(e -> this.clean());
        this.mainForm.textareaConvertCommonDecoded.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                flagCanDecode.set(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                flagCanDecode.set(true);
            }
        });
        this.mainForm.textareaConvertCommonDecoded.getDocument().addDocumentListener(new DocumentListener() {
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
        this.mainForm.textareaConvertCommonEncoded.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                flagCanEncode.set(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                flagCanEncode.set(true);
            }
        });
        this.mainForm.textareaConvertCommonEncoded.getDocument().addDocumentListener(new DocumentListener() {
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
        this.mainForm.selectConvertCommonCharset.addItemListener(e -> redo());
        this.mainForm.radioConvertCommonBase64.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonHex.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonHtml.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonUnicode.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonUriComponent.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonJson.addActionListener(this::radioChanged);
        this.mainForm.radioConvertCommonTime.addActionListener(this::radioChanged);
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollConvertCommonDecoded, this.mainForm.scrollConvertCommonEncoded);
        this.mainForm.scrollConvertCommonDecoded.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollConvertCommonDecoded.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollConvertCommonEncoded.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollConvertCommonEncoded.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void radioChanged(ActionEvent e) {
        if (((JRadioButton) e.getSource()).isSelected()) {
            redo();
        }
    }

    private void redo() {
        if (this.lastCommandIsEncode.get()) {
            flagCanDecode.set(false);
            encode();
        } else {
            flagCanEncode.set(false);
            decode();
        }
    }

    private String getCharset() {
        int encodingModelIndex = this.mainForm.selectConvertCommonCharset.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectConvertCommonCharset.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private void autoEncode() {
        if (this.mainForm.checkConvertCommonAuto.isSelected()) {
            encode();
        }
    }

    private void clean() {
        flagCanEncode.set(false);
        flagCanDecode.set(false);
        this.mainForm.textareaConvertCommonEncoded.setText("");
        this.mainForm.textareaConvertCommonDecoded.setText("");
        flagCanEncode.set(true);
        flagCanDecode.set(true);
    }

    private void encode() {
        String decoded = this.mainForm.textareaConvertCommonDecoded.getText();
        if (decoded == null || decoded.trim().length() == 0) {
            return;
        }
        if (this.mainForm.checkConvertCommonLine.isSelected()) {
            String[] split = decoded.replace("\r", "").split("\n");
            StringBuilder sb = new StringBuilder();
            for (String eachLine : split) {
                String encoded;
                try {
                    encoded = encode(eachLine);
                } catch (Exception e) {
                    encoded = MessageUtil.getMessage("convert.encode.fail", e.getClass().getName(), e.getLocalizedMessage());
                }
                sb.append(encoded).append("\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            this.mainForm.textareaConvertCommonEncoded.setText(result);
        } else {
            String encoded;
            try {
                encoded = encode(decoded);
            } catch (Exception e) {
                encoded = MessageUtil.getMessage("convert.encode.fail", e.getClass().getName(), e.getLocalizedMessage());
            }
            this.mainForm.textareaConvertCommonEncoded.setText(encoded);
        }
    }

    private String encode(String decoded) throws UnsupportedEncodingException, ParseException {
        if (decoded == null || decoded.trim().length() == 0) {
            return "";
        }
        if (this.mainForm.radioConvertCommonBase64.isSelected()) {
            return new String(Base64.getEncoder().encode(decoded.getBytes(getCharset())), getCharset());
        } else if (this.mainForm.radioConvertCommonHex.isSelected()) {
            if (decoded.startsWith("0x")) {
                // HEX 从十六进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 16);
                return MessageUtil.getMessage("convert.encode.hex", l, Long.toOctalString(l), Long.toBinaryString(l));
            } else if (decoded.startsWith("0o")) {
                // OCT 从八进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 8);
                return MessageUtil.getMessage("convert.encode.oct", l, Long.toHexString(l), Long.toBinaryString(l));
            } else if (decoded.startsWith("0b")) {
                // BIN 从二进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 2);
                return MessageUtil.getMessage("convert.encode.bin", l, Long.toHexString(l), Long.toOctalString(l));
            } else if (decoded.startsWith("0d")) {
                // DEC 从十进制转换
                decoded = decoded.substring(2);
                long l = Long.parseLong(decoded, 10);
                return MessageUtil.getMessage("convert.encode.dec", Long.toHexString(l), Long.toOctalString(l), Long.toBinaryString(l));
            } else {
                // 文本转换十六进制
                byte[] decodedBytes = decoded.getBytes(getCharset());
                return StrUtil.bytesToHex(decodedBytes, getCharset());
            }
        } else if (this.mainForm.radioConvertCommonHtml.isSelected()) {
            return StringEscapeUtils.escapeHtml4(decoded);
        } else if (this.mainForm.radioConvertCommonUnicode.isSelected()) {
            return StringEscapeUtils.escapeJava(decoded);
        } else if (this.mainForm.radioConvertCommonUriComponent.isSelected()) {
            return URLEncoder.encode(decoded, getCharset())
                    .replace("+", "%20").replace("%21", "!")
                    .replace("%28", "(").replace("%29", ")")
                    .replace("%7E", "~").replace("%27", "'");
        } else if (this.mainForm.radioConvertCommonJson.isSelected()) {
            return StringEscapeUtils.escapeJson(decoded);
        } else if (this.mainForm.radioConvertCommonTime.isSelected()) {
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
                        return MessageUtil.getMessage("convert.encode.time", parse.getTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").format(parse));
                    }
                }
                DateTime dateTime = DateUtil.parse(decoded);
                return MessageUtil.getMessage("convert.encode.time", dateTime.getTime(), dateTime.toString("yyyy-MM-dd HH:mm:ss.SSS z"));
            }
        }
        throw new IllegalArgumentException(MessageUtil.getMessage("convert.encode.not.select.type"));
    }

    private void autoDecode() {
        if (this.mainForm.checkConvertCommonAuto.isSelected()) {
            decode();
        }
    }

    private void decode() {
        String encoded = this.mainForm.textareaConvertCommonEncoded.getText();
        if (encoded == null || encoded.trim().length() == 0) {
            return;
        }
        if (this.mainForm.checkConvertCommonLine.isSelected()) {
            String[] split = encoded.replace("\r", "").split("\n");
            StringBuilder sb = new StringBuilder();
            for (String eachLine : split) {
                String decoded;
                try {
                    decoded = decode(eachLine);
                } catch (Exception e) {
                    decoded = MessageUtil.getMessage("convert.decode.fail", e.getClass().getName(), e.getLocalizedMessage());
                }
                sb.append(decoded).append("\n");
            }
            String result = sb.toString();
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
            this.mainForm.textareaConvertCommonDecoded.setText(result);
        } else {
            String decoded;
            try {
                decoded = encode(encoded);
            } catch (Exception e) {
                decoded = MessageUtil.getMessage("convert.decode.fail", e.getClass().getName(), e.getLocalizedMessage());
            }
            this.mainForm.textareaConvertCommonDecoded.setText(decoded);
        }
    }

    private String decode(String encoded) throws UnsupportedEncodingException {
        if (encoded == null || encoded.trim().length() == 0) {
            return "";
        }
        if (this.mainForm.radioConvertCommonBase64.isSelected()) {
            return new String(Base64.getDecoder().decode(encoded.getBytes(getCharset())), getCharset());
        } else if (this.mainForm.radioConvertCommonHex.isSelected()) {
            // TODO test
            return new String(HexUtil.decodeHex(encoded), getCharset());
        } else if (this.mainForm.radioConvertCommonHtml.isSelected()) {
            return StringEscapeUtils.unescapeHtml4(encoded);
        } else if (this.mainForm.radioConvertCommonUnicode.isSelected()) {
            return StringEscapeUtils.unescapeJava(encoded);
        } else if (this.mainForm.radioConvertCommonUriComponent.isSelected()) {
            return URLDecoder.decode(encoded
                            .replace("%20", "+").replace("!", "%21")
                            .replace("(", "%28").replace(")", "%29")
                            .replace("~", "%7E").replace("'", "%27")
                    , getCharset());
        } else if (this.mainForm.radioConvertCommonJson.isSelected()) {
            return StringEscapeUtils.unescapeJson(encoded);
        } else if (this.mainForm.radioConvertCommonTime.isSelected()) {
            throw new IllegalArgumentException(MessageUtil.getMessage("convert.decode.time.not.support"));
        }
        throw new IllegalArgumentException(MessageUtil.getMessage("convert.decode.not.select.type"));
    }

}
