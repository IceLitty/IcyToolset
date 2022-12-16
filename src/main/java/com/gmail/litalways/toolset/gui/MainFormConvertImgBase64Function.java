package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.enums.KeyEnum;
import com.gmail.litalways.toolset.util.StrUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * @author IceRain
 * @since 2022/01/20
 */
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
        FileChooserDescriptor descriptor;
        if ((ActionEvent.CTRL_MASK & e.getModifiers()) != 0) {
            // 选择文件夹批量模式
            descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            descriptor.setTitle("Select Directory Contains Binary File:");
        } else {
            // 选择单个文件
            descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            descriptor.setTitle("Select Binary File:");
        }
        this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
        encodeToString();
    }

    private String encodeToString() {
        clean();
        if (this.toSelect != null) {
            this.mainForm.fileConvertImgBase64Path.setText(this.toSelect.getPath());
            if (this.toSelect.isDirectory()) {
                int encodingModelIndex = this.mainForm.selectConvertImgBase64Charset.getSelectedIndex();
                Object selectedObjects = this.mainForm.selectConvertImgBase64Charset.getModel().getSelectedItem();
                String charset;
                if (encodingModelIndex == 0) {
                    charset = System.getProperty("file.encoding");
                } else {
                    charset = (String) selectedObjects;
                }
                File srcDir = new File(this.toSelect.getPath());
                encodeToFileMulti(srcDir, charset);
                return null;
            }
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

    private void encodeToFileMulti(File dir, String charset) {
        if (dir == null || !dir.isDirectory() || !dir.exists() || !dir.canRead() || !dir.canWrite() || dir.listFiles() == null || dir.listFiles().length == 0) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("No source dir selected.", null, dir == null ? null : dir.getPath(), NotificationType.WARNING)
                    .notify(null);
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                byte[] bytes;
                try (InputStream fileIs = this.toSelect.getInputStream()) {
                    bytes = fileIs.readAllBytes();
                } catch (Exception ex) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
                }
                if (bytes == null || bytes.length == 0) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("File is empty.", null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
                }
                byte[] encode = Base64.getEncoder().encode(bytes);
                String base64 = new String(encode);
                String destPath;
                if (f.getName().contains(".")) {
                    String path = f.getPath();
                    destPath = path.substring(0, path.lastIndexOf(".")) + ".txt";
                } else {
                    destPath = f.getPath() + ".txt";
                }
                File file = new File(destPath);
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
                            .createNotification(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    return;
                }
            } else if (f.isDirectory()) {
                encodeToFileMulti(f, charset);
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
            FileChooserDescriptor descriptor;
            if ((ActionEvent.CTRL_MASK & e.getModifiers()) != 0) {
                // 选择文件夹批量模式
                descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select Source Directory Contains BASE64 File:");
            } else {
                // 选择单个文件
                descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
                descriptor.setTitle("Select Source File Contains BASE64 Code:");
            }
            VirtualFile src = FileChooser.chooseFile(descriptor, null, null);
            if (src == null) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("No source file selected.", null, null, NotificationType.ERROR)
                        .notify(null);
                return;
            }
            if (src.isDirectory()) {
                // 选择文件夹批量模式
                decodeToFileMulti(new File(src.getPath()), charset);
                return;
            } else {
                // 选择单个文件
                try (InputStreamReader isr = new InputStreamReader(src.getInputStream(), Charset.forName(charset));
                     BufferedReader br = new BufferedReader(isr)) {
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

    private void decodeToFileMulti(File dir, String charset) {
        if (dir == null || !dir.isDirectory() || !dir.exists() || !dir.canRead() || !dir.canWrite() || dir.listFiles() == null || dir.listFiles().length == 0) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("No base64 dir selected.", null, dir == null ? null : dir.getPath(), NotificationType.WARNING)
                    .notify(null);
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                String suffix;
                String text;
                try (FileInputStream fis = new FileInputStream(f);
                     InputStreamReader isr = new InputStreamReader(fis, Charset.forName(charset));
                     BufferedReader br = new BufferedReader(isr)) {
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
                            .createNotification(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
                }
                if (text.startsWith("JVBER")) {
                    suffix = ".pdf";
                } else if (text.startsWith("iVBOR")) {
                    suffix = ".png";
                } else if (text.startsWith("/9j/4")) {
                    suffix = ".jpg";
                } else if (text.startsWith("Qk0")) {
                    suffix = ".bmp";
                } else {
                    suffix = ".bin";
                }
                String destPath;
                if (f.getName().contains(".")) {
                    String path = f.getPath();
                    destPath = path.substring(0, path.lastIndexOf(".")) + suffix;
                } else {
                    destPath = f.getPath() + suffix;
                }
                if (text == null || text.trim().length() == 0) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Not source BASE64 text.", null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
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
                            .createNotification("Wrong charset or Base64 decode failed.", null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
                }
                try {
                    File targetFile = new File(destPath);
                    if (!targetFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        targetFile.createNewFile();
                    }
                    try (FileOutputStream fos = new FileOutputStream(targetFile, false)) {
                        fos.write(decode);
                    }
                } catch (Exception ex) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), null, f.getPath(), NotificationType.ERROR)
                            .notify(null);
                    continue;
                }
            } else if (f.isDirectory()) {
                decodeToFileMulti(f, charset);
            }
        }
    }

}
