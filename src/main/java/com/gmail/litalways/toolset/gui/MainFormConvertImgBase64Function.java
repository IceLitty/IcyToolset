package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.enums.KeyEnum;
import com.gmail.litalways.toolset.util.StrUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * @author IceRain
 * @since 2022/01/20
 */
@Slf4j
public class MainFormConvertImgBase64Function {

    private final MainForm mainForm;
    private VirtualFile toSelect = null;

    public MainFormConvertImgBase64Function(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.fileConvertImgBase64Path.addActionListener(this::encodeToString);
        this.mainForm.selectConvertImgBase64Charset.addItemListener(e -> this.encodeToString());
        this.mainForm.buttonConvertImgBase64Encode.addActionListener(e -> this.encodeToFile(this.encodeToString()));
        this.mainForm.buttonConvertImgBase64Decode.addActionListener(this::decodeToFile);
        this.mainForm.buttonConvertImgBase64Clean.addActionListener(e -> this.clean());
    }

    private void clean() {
        this.mainForm.textareaConvertImgBase64.setText("");
    }

    private void encodeToString(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
        this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
        encodeToString();
    }

    private String encodeToString() {
        if (this.toSelect != null) {
            this.mainForm.fileConvertImgBase64Path.setText(this.toSelect.getPath());
            byte[] bytes;
            try (InputStream fileIs = this.toSelect.getInputStream()) {
                bytes = fileIs.readAllBytes();
            } catch (Exception ex) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
                return null;
            }
            byte[] encode = Base64.getEncoder().encode(bytes);
            int encodingModelIndex = this.mainForm.selectConvertImgBase64Charset.getSelectedIndex();
            Object selectedObjects = this.mainForm.selectConvertImgBase64Charset.getModel().getSelectedItem();
            String text;
            if (encodingModelIndex == 0) {
                text = new String(encode);
            } else {
                try {
                    text = new String(encode, (String) selectedObjects);
                } catch (Exception ex) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                            .notify(null);
                    return null;
                }
            }
            this.mainForm.textareaConvertImgBase64.setText(StrUtil.showMax(text, 2000));
            return text;
        }
        return null;
    }

    private void encodeToFile(String base64) {
        if (base64 == null || base64.trim().length() == 0) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("No source BASE64 text.", null, null, NotificationType.ERROR)
                    .notify(null);
            return;
        }
        JFileChooser fileChooser = new JFileChooser(this.toSelect == null ? null : this.toSelect.getPath());
        int status = fileChooser.showSaveDialog(this.mainForm.panelMain);
        if (status == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (!file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }
                try (FileWriter fw = new FileWriter(file, false)) {
                    fw.write(base64);
                }
            } catch (Exception ex) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
                return;
            }
        }
    }

    private void decodeToFile(ActionEvent e) {
        String destPath = null;
        String text = this.mainForm.textareaConvertImgBase64.getText();
        boolean needReSelectFile = text == null || text.trim().length() == 0 || StrUtil.endsWithShowMax(text);
        if (needReSelectFile) {
            int encodingModelIndex = this.mainForm.selectConvertImgBase64Charset.getSelectedIndex();
            Object selectedObjects = this.mainForm.selectConvertImgBase64Charset.getModel().getSelectedItem();
            String charset;
            if (encodingModelIndex == 0) {
                charset = System.getProperty("file.encoding");
            } else {
                charset = (String) selectedObjects;
            }
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
            descriptor.setTitle("Select Source File Contain BASE64 Code:");
            VirtualFile src = FileChooser.chooseFile(descriptor, null, null);
            if (src == null) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("No source file selected.", null, null, NotificationType.ERROR)
                        .notify(null);
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(src.getInputStream(), Charset.forName(charset)))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                text = sb.toString();
                if (text.endsWith("\n")) {
                    text = text.substring(0, text.length() - 1);
                }
            } catch (Exception ex) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
                return;
            }
            destPath = src.getPath();
        }
        if (destPath == null && this.toSelect != null) {
            destPath = this.toSelect.getPath();
        }
        if (text == null || text.trim().length() == 0) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("No source BASE64 text.", null, null, NotificationType.ERROR)
                    .notify(null);
            return;
        }
        int encodingModelIndex = this.mainForm.selectConvertImgBase64Charset.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectConvertImgBase64Charset.getModel().getSelectedItem();
        byte[] decode;
        try {
            byte[] bytes;
            if (encodingModelIndex == 0) {
                bytes = text.getBytes();
            } else {
                bytes = text.getBytes((String) selectedObjects);
            }
            decode = Base64.getDecoder().decode(bytes);
        } catch (Exception ex) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                    .notify(null);
            return;
        }
        JFileChooser fileChooser = new JFileChooser(destPath);
        int status = fileChooser.showSaveDialog(this.mainForm.panelMain);
        if (status == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (!file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    fos.write(decode);
                }
            } catch (Exception ex) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
                return;
            }
        }
    }

}
