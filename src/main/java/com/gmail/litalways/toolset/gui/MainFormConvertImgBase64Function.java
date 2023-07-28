package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.util.ExplorerUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.gmail.litalways.toolset.util.StrUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * @author IceRain
 * @since 2022/01/20
 */
public class MainFormConvertImgBase64Function {

    private final ToolWindowConvert component;
    private VirtualFile toSelect = null;

    public MainFormConvertImgBase64Function(ToolWindowConvert component) {
        this.component = component;
        this.component.fileConvertImgBase64Path.addActionListener(this::encodeToStringByFileComponent);
        this.component.buttonConvertImgBase64Encode.addActionListener(e -> this.encodeToStringByOtherComponent());
        this.component.buttonConvertImgBase64Decode.addActionListener(this::decodeToFileByOtherComponent);
        this.component.buttonConvertImgBase64Clean.addActionListener(e -> this.clean());
    }

    private void clean() {
        this.toSelect = null;
        this.component.fileConvertImgBase64Path.setText("");
        this.component.textareaConvertImgBase64.setText("");
    }

    /**
     * 编码二进制文件到BASE64
     *
     * @param e 事件
     */
    private void encodeToStringByFileComponent(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, false);
        descriptor.setTitle(MessageUtil.getMessage("convert.base64.tip.select.bin"));
        this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
        if (this.toSelect != null) {
            this.component.fileConvertImgBase64Path.setText(this.toSelect.getPath());
            if (this.toSelect.isDirectory()) {
                File srcDir = new File(this.toSelect.getPath());
                encodeToFileMulti(srcDir);
                if (this.component.checkConvertImgBase64OpenDirectory.isSelected()) {
                    try {
                        ExplorerUtil.openExplorer(srcDir.getPath());
                    } catch (IOException ex) {
                        NotificationUtil.error(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), srcDir.getPath());
                    }
                }
            } else {
                encodeToString();
            }
        } else {
            this.component.fileConvertImgBase64Path.setText("");
        }
    }

    private void encodeToStringByOtherComponent() {
        if (this.toSelect != null) {
            if (this.toSelect.isDirectory()) {
                File srcDir = new File(this.toSelect.getPath());
                encodeToFileMulti(srcDir);
                if (this.component.checkConvertImgBase64OpenDirectory.isSelected()) {
                    try {
                        ExplorerUtil.openExplorer(srcDir.getPath());
                    } catch (IOException ex) {
                        NotificationUtil.error(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), srcDir.getPath());
                    }
                }
            } else {
                encodeToFile(encodeToString());
            }
        }
    }

    /**
     * 编码二进制文件到BASE64
     *
     * @return BASE64
     */
    private String encodeToString() {
        clean();
        if (this.toSelect != null) {
            this.component.fileConvertImgBase64Path.setText(this.toSelect.getPath());
            byte[] bytes;
            try (InputStream fileIs = this.toSelect.getInputStream()) {
                bytes = fileIs.readAllBytes();
            } catch (Exception ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                return null;
            }
            byte[] encode = Base64.getEncoder().encode(bytes);
            int encodingModelIndex = this.component.selectConvertImgBase64Charset.getSelectedIndex();
            Object selectedObjects = this.component.selectConvertImgBase64Charset.getModel().getSelectedItem();
            String text;
            if (encodingModelIndex == 0) {
                text = new String(encode);
            } else {
                try {
                    text = new String(encode, (String) selectedObjects);
                } catch (Exception ex) {
                    NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                    return null;
                }
            }
            this.component.textareaConvertImgBase64.setText(StrUtil.showMax(text, 2000));
            return text;
        }
        return null;
    }

    /**
     * 编码二进制文件到BASE64文件
     *
     * @param base64 BASE64
     */
    private void encodeToFile(String base64) {
        if (base64 == null || base64.trim().length() == 0) {
            NotificationUtil.error(MessageUtil.getMessage("convert.base64.tip.not.base64"));
            return;
        }
        JFileChooser fileChooser = new JFileChooser(this.toSelect == null ? null : this.toSelect.getPath());
        int status = fileChooser.showSaveDialog(this.component.panelMain);
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
                if (this.component.checkConvertImgBase64OpenDirectory.isSelected()) {
                    ExplorerUtil.openExplorerAndHighlightFile(file);
                }
            } catch (Exception ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            }
        }
    }

    /**
     * 编码二进制文件到多个BASE64文件
     *
     * @param dir 二进制文件根目录
     */
    private void encodeToFileMulti(File dir) {
        File[] dirListFiles;
        if (dir == null || !dir.isDirectory() || !dir.exists() || !dir.canRead() || !dir.canWrite() || (dirListFiles = dir.listFiles()) == null || dirListFiles.length == 0) {
            NotificationUtil.warning(MessageUtil.getMessage("convert.base64.tip.not.select.bin.dir"), dir == null ? null : dir.getPath());
            return;
        }
        // 检查是否具有重名文件
        boolean lessSuffix = true;
        Set<String> fileNameWithoutSuffix = new HashSet<>();
        for (File f : dirListFiles) {
            String fileName = f.getName();
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileNameWithoutSuffix.add(fileName);
        }
        if (fileNameWithoutSuffix.size() != dirListFiles.length) {
            lessSuffix = false;
        }
        for (File f : dirListFiles) {
            if (f.isFile()) {
                byte[] bytes;
                try (InputStream fileIs = new FileInputStream(f)) {
                    bytes = fileIs.readAllBytes();
                } catch (Exception ex) {
                    NotificationUtil.warning(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), f.getPath());
                    continue;
                }
                if (bytes == null || bytes.length == 0) {
                    NotificationUtil.warning(MessageUtil.getMessage("convert.base64.tip.file.empty"), f.getPath());
                    continue;
                }
                byte[] encode = Base64.getEncoder().encode(bytes);
                String base64 = new String(encode);
                String destPath;
                if (lessSuffix) {
                    if (f.getName().contains(".")) {
                        String path = f.getPath();
                        destPath = path.substring(0, path.lastIndexOf(".")) + ".txt";
                        if (new File(destPath).exists()) {
                            destPath = path + ".txt";
                        }
                    } else {
                        destPath = f.getPath() + ".txt";
                    }
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
                    NotificationUtil.error(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), f.getPath());
                    return;
                }
            } else if (f.isDirectory()) {
                encodeToFileMulti(f);
            }
        }
    }

    private void decodeToFileByOtherComponent(ActionEvent e) {
        String text = this.component.textareaConvertImgBase64.getText();
        int encodingModelIndex = this.component.selectConvertImgBase64Charset.getSelectedIndex();
        Object selectedObjects = this.component.selectConvertImgBase64Charset.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        boolean needReSelectFile = text == null || text.trim().length() == 0 || StrUtil.endsWithShowMax(text);
        if (needReSelectFile) {
//            // 由于文件选择控件不支持该识别，所以按钮这边也同步放弃该方案
//            FileChooserDescriptor descriptor;
//            if ((ActionEvent.CTRL_MASK & e.getModifiers()) != 0) {
//                // 选择文件夹批量模式
//                descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
//                descriptor.setTitle(MessageUtil.getMessage("convert.base64.tip.select.base64.dir"));
//            } else {
//                // 选择单个文件
//                descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
//                descriptor.setTitle(MessageUtil.getMessage("convert.base64.tip.select.base64.file"));
//            }
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, false);
            descriptor.setTitle(MessageUtil.getMessage("convert.base64.tip.select.base64"));
            VirtualFile src = FileChooser.chooseFile(descriptor, null, null);
            if (src == null) {
                NotificationUtil.error(MessageUtil.getMessage("convert.base64.tip.not.select.bin.file"));
                return;
            }
            if (src.isDirectory()) {
                // 选择文件夹批量模式
                decodeToFileMulti(new File(src.getPath()), charset);
                if (this.component.checkConvertImgBase64OpenDirectory.isSelected()) {
                    try {
                        ExplorerUtil.openExplorer(src.getPath());
                    } catch (IOException ex) {
                        NotificationUtil.error(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), src.getPath());
                    }
                }
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
                    NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                    return;
                }
                decodeToFile(text, src.getPath(), charset);
            }
        } else {
            // 选择单个文件
            decodeToFile(text, null, charset);
        }
    }

    /**
     * 解码BASE64至文件
     *
     * @param base64   BASE64
     * @param destPath 目标文件夹
     * @param charset  字符集
     */
    private void decodeToFile(String base64, String destPath, String charset) {
        if (base64.trim().length() == 0) {
            NotificationUtil.error(MessageUtil.getMessage("convert.base64.tip.not.base64"), destPath);
            return;
        }
        byte[] decode;
        try {
            byte[] bytes = base64.getBytes(charset);
            decode = Base64.getDecoder().decode(bytes);
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            return;
        }
        JFileChooser fileChooser = new JFileChooser(destPath);
        int status = fileChooser.showSaveDialog(this.component.panelMain);
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
                if (this.component.checkConvertImgBase64OpenDirectory.isSelected()) {
                    ExplorerUtil.openExplorerAndHighlightFile(file);
                }
            } catch (Exception ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            }
        }
    }

    /**
     * 解码BASE64至多个文件
     *
     * @param dir     BASE64根目录
     * @param charset 字符集
     */
    @SuppressWarnings("UnnecessaryContinue")
    private void decodeToFileMulti(File dir, String charset) {
        File[] dirListFiles;
        if (dir == null || !dir.isDirectory() || !dir.exists() || !dir.canRead() || !dir.canWrite() || (dirListFiles = dir.listFiles()) == null || dirListFiles.length == 0) {
            NotificationUtil.error(MessageUtil.getMessage("convert.base64.tip.not.select.base64.dir"), dir == null ? null : dir.getPath());
            return;
        }
        // 检查是否具有重名文件
        boolean lessSuffix = true;
        Set<String> fileNameWithoutSuffix = new HashSet<>();
        for (File f : dirListFiles) {
            String fileName = f.getName();
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileNameWithoutSuffix.add(fileName);
        }
        if (fileNameWithoutSuffix.size() != dirListFiles.length) {
            lessSuffix = false;
        }
        for (File f : dirListFiles) {
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
                    NotificationUtil.warning(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), f.getPath());
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
                if (lessSuffix) {
                    if (f.getName().contains(".")) {
                        String path = f.getPath();
                        destPath = path.substring(0, path.lastIndexOf(".")) + suffix;
                        if (new File(destPath).exists()) {
                            destPath = path + suffix;
                        }
                    } else {
                        destPath = f.getPath() + suffix;
                    }
                } else {
                    destPath = f.getPath() + suffix;
                }
                if (text.trim().length() == 0) {
                    NotificationUtil.warning(MessageUtil.getMessage("convert.base64.tip.not.base64"), f.getPath());
                    continue;
                }
                int encodingModelIndex = this.component.selectConvertImgBase64Charset.getSelectedIndex();
                Object selectedObjects = this.component.selectConvertImgBase64Charset.getModel().getSelectedItem();
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
                    NotificationUtil.warning(MessageUtil.getMessage("convert.base64.tip.wrong.charset.or.base64"), f.getPath());
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
                    NotificationUtil.warning(ex.getClass().getName() + ": " + ex.getLocalizedMessage(), f.getPath());
                    continue;
                }
            } else if (f.isDirectory()) {
                decodeToFileMulti(f, charset);
            }
        }
    }

}
