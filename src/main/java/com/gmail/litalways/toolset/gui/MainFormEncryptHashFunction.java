package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.HMac;
import com.gmail.litalways.toolset.filter.ManifestFileFilter;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.ExplorerUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IceRain
 * @since 2022/01/27
 */
public class MainFormEncryptHashFunction {

    private final ToolWindowEncrypt component;
    private VirtualFile[] toSelects = new VirtualFile[0];
    private VirtualFile[] toSelectAssets = new VirtualFile[0];
    private Integer lastFunction = null;

    public MainFormEncryptHashFunction(ToolWindowEncrypt component) {
        this.component = component;
        this.component.buttonEncryptHashManifest.setIcon(AllIcons.General.InlineRefreshHover);
        this.component.buttonEncryptHashManifest.addActionListener(this::generateManifest);
        this.component.buttonEncryptHashOpenDirectory.setIcon(AllIcons.Nodes.Folder);
        this.component.buttonEncryptHashOpenDirectory.addActionListener(e -> {
            try {
                if (this.toSelects.length == 1) {
                    ExplorerUtil.openExplorerAndHighlightFile(this.toSelects[0]);
                } else if (this.toSelects.length > 1) {
                    ExplorerUtil.openExplorer(this.toSelects[0]);
                }
            } catch (IOException ex) {
                NotificationUtil.error(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
            }
        });
        this.component.buttonEncryptHashAssertsOpenDirectory.setIcon(AllIcons.Nodes.Folder);
        this.component.buttonEncryptHashAssertsOpenDirectory.addActionListener(e -> {
            try {
                if (this.toSelectAssets.length == 1) {
                    ExplorerUtil.openExplorerAndHighlightFile(this.toSelectAssets[0]);
                } else if (this.toSelectAssets.length > 1) {
                    ExplorerUtil.openExplorer(this.toSelectAssets[0]);
                }
            } catch (IOException ex) {
                NotificationUtil.error(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
            }
        });
        this.component.buttonEncryptHashGenerateKey.addActionListener(this::generateHMacKey);
        this.component.buttonEncryptHashClean.addActionListener(e -> this.clean());
        this.component.fileEncryptHashFile.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, true);
            this.toSelects = FileChooser.chooseFiles(descriptor, null, this.toSelects.length == 0 ? null : this.toSelects[this.toSelects.length - 1]);
            if (this.toSelects.length == 0) {
                this.component.fileEncryptHashFile.setText("");
                return;
            }
            this.component.fileEncryptHashFile.setText(this.toSelects[this.toSelects.length - 1].getPresentableUrl() + (this.toSelects.length == 1 ? "" : (" ... and " + (this.toSelects.length - 1) + " files.")));
            // Calc size and tips
            for (VirtualFile file : this.toSelects) {
                if (file.getLength() >= 524288000) {
                    NotificationUtil.warning(MessageUtil.getMessage("encrypt.hash.tip.large.file"), "\"certutil -hashfile src.bin MD5 >> hash.txt\"");
                }
            }
            this.doHash(0);
        });
        this.component.fileEncryptHashAsserts.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, true);
            this.toSelectAssets = FileChooser.chooseFiles(descriptor, null, this.toSelectAssets.length == 0 ? null : this.toSelectAssets[this.toSelectAssets.length - 1]);
            if (this.toSelectAssets.length == 0) {
                this.component.fileEncryptHashAsserts.setText("");
                return;
            }
            this.component.fileEncryptHashAsserts.setText(this.toSelectAssets[this.toSelectAssets.length - 1].getPresentableUrl() + (this.toSelectAssets.length == 1 ? "" : (" ... and " + (this.toSelectAssets.length - 1) + " files.")));
            if (this.toSelectAssets.length != this.toSelects.length) {
                NotificationUtil.warning(MessageUtil.getMessage("encrypt.hash.tip.compare.files.count.not.equal.bin.files"));
            }
            this.component.textareaEncryptHashText.setText("");
            this.doHash(0);
        });
        this.component.textareaEncryptHashText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doHash(1);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                doHash(1);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        this.component.buttonEncryptHashFile.addActionListener(e -> this.doHash(0));
        this.component.buttonEncryptHashText.addActionListener(e -> this.doHash(1));
        this.component.selectEncryptHashEncoding.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        this.component.selectEncryptHashType.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        this.component.selectEncryptHashOutputType.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.component.scrollEncryptHashText, this.component.scrollEncryptHashResult);
        this.component.scrollEncryptHashText.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptHashText.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptHashResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptHashResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void clean() {
        this.component.textareaEncryptHashText.setText("");
        this.component.textareaEncryptHashResult.setText("");
        this.component.fileEncryptHashAsserts.setText("");
        this.toSelectAssets = new VirtualFile[0];
        this.component.textareaEncryptHashText.setText("");
        this.component.textEncryptHashKey.setText("");
    }

    private void generateManifest(ActionEvent event) {
        if (toSelects == null || toSelects.length == 0) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser(toSelects[0].getPath());
        fileChooser.setSelectedFile(new File("MANIFEST.MF"));
        fileChooser.setFileFilter(new ManifestFileFilter());
        int status = fileChooser.showSaveDialog(component.panelMain);
        if (status == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            ProgressManager.getInstance().run(new Task.Backgroundable(this.component.getCurrentProject(), MessageUtil.getMessage("encrypt.hash.tip.running")) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        StringBuilder manifestBuilder = new StringBuilder("Manifest-Version: 1.0\nCreated-By: IcyToolset\n");
                        VirtualFile[] selects = toSelects;
                        for (int i = 0, selectsLength = selects.length; i < selectsLength; i++) {
                            VirtualFile toSelect = selects[i];
                            if (toSelect.isDirectory()) {
                                manifestBuilder.append("IcyToolset-PathPrefix-").append(i + 1).append(": ").append(toSelect.getPresentableUrl()).append("\n");
                            }
                        }
                        manifestBuilder.append("\n");
                        AtomicLong pgInFinished = new AtomicLong(0);
                        AtomicLong pgInMax = new AtomicLong(toSelects.length);
                        for (VirtualFile toSelect : toSelects) {
                            _generateManifest(toSelect, manifestBuilder, progressIndicator, pgInFinished, pgInMax);
                            progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                        }
                        Files.writeString(file.toPath(), manifestBuilder.toString(), getCharset(), StandardOpenOption.CREATE);
                        NotificationUtil.info(MessageUtil.getMessage("encrypt.hash.tip.done"));
                    } catch (Exception ex) {
                        NotificationUtil.error(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void _generateManifest(VirtualFile file, StringBuilder out, ProgressIndicator progressIndicator, AtomicLong pgInFinished, AtomicLong pgInMax) {
        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            pgInMax.addAndGet(children.length);
            for (VirtualFile child : children) {
                _generateManifest(child, out, progressIndicator, pgInFinished, pgInMax);
                progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
            }
        } else {
            Digester digester = DigestUtil.digester("SHA1");
            File _file = new File(file.getPath());
            // 不符合路径正则条件的过滤掉，不进行计算和输出
            String filterRegex = this.component.textEncryptHashPathNameFilter.getText();
            if (filterRegex != null && !filterRegex.trim().isEmpty()) {
                Matcher matcher = Pattern.compile(filterRegex).matcher(file.getPresentableUrl());
                if (filterRegex.startsWith("^") || filterRegex.endsWith("$")) {
                    if (!matcher.matches()) {
                        return;
                    }
                } else {
                    if (!matcher.find()) {
                        return;
                    }
                }
            }
            String resultHex = digester.digestHex(_file);
            resultHex = new String(Base64.getEncoder().encode(HexUtil.decodeHex(resultHex)), StandardCharsets.UTF_8);
            out.append("Name: ").append(file.getPresentableUrl()).append("\nSHA1-Digest: ").append(resultHex).append("\n\n");
        }
    }

    private DigestAssert getAssertStr(VirtualFile source) {
        // 单结果比对模式
        String assertStr = this.component.textEncryptHashAssert.getText();
        if (assertStr != null && !assertStr.trim().isEmpty()) {
            return new DigestAssert(assertStr, null, null);
        }
        // 一对一正常比对模式
        if (source == null || this.toSelectAssets.length == 0) {
            return null;
        }
        if (this.toSelectAssets.length == 1 && "MANIFEST.MF".equals(this.toSelectAssets[0].getName())) {
            // 通过清单文件比对hash
            String manifest;
            try (InputStream fileIs = this.toSelectAssets[0].getInputStream()) {
                manifest = new String(fileIs.readAllBytes(), getCharset());
            } catch (Exception ex) {
                return null;
            }
            String[] arr = manifest.replace("\r", "").split("\n");
            List<String> pathPrefix = new ArrayList<>();
            boolean matchLine = false;
            for (String line : arr) {
                if (line.startsWith("IcyToolset-PathPrefix-")) {
                    String pathPrefix1 = line.substring(line.indexOf(": ") + 2);
                    if (!pathPrefix1.trim().isEmpty()) {
                        pathPrefix.add(pathPrefix1);
                    }
                } else if (matchLine) {
                    // SHA1-Digest: BASE64
                    String[] split = line.split("-Digest: ");
                    if (split.length != 2) {
                        return null;
                    }
                    String base64 = split[split.length - 1];
                    return new DigestAssert(base64, split[0].toUpperCase(), "Base64");
                } else {
                    // Name: path-to-file.suffix
                    if (line.startsWith("Name:")) {
                        String[] splitLine = line.replace("\\", "/").split("/");
                        String fileNameWithSuffix = splitLine[splitLine.length - 1];
                        String sourceName = source.getName();
                        for (String prefix : pathPrefix) {
                            if (fileNameWithSuffix.startsWith(prefix) && sourceName.startsWith(prefix)) {
                                fileNameWithSuffix = fileNameWithSuffix.substring(prefix.length());
                                sourceName = sourceName.substring(prefix.length());
                                break;
                            }
                        }
                        // boo.txt <=> foo.txt
                        if (sourceName.equals(fileNameWithSuffix)) {
                            matchLine = true;
                        }
                    }
                }
            }
            return null;
        } else {
            // 通过同名sha1文件比对hash
            VirtualFile assertTarget = null;
            String name = source.getNameWithoutExtension();
            String name2 = source.getName();
            for (VirtualFile file : this.toSelectAssets) {
                // boo(.txt) <=> foo(.sha1)
                // boo.txt <=> foo.txt(.sha1)
                if (name.equals(file.getNameWithoutExtension()) || name2.equals(file.getNameWithoutExtension())) {
                    assertTarget = file;
                    break;
                }
            }
            if (assertTarget == null) {
                return null;
            }
            try (InputStream fileIs = assertTarget.getInputStream()) {
                String s = new String(fileIs.readAllBytes(), getCharset());
                String extension = assertTarget.getExtension();
                if (extension != null) {
                    extension = extension.toUpperCase();
                }
                return new DigestAssert(s, extension, null);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * @param sourceType 0修改文件触发（文件hash） 1修改文本框触发（文本hash）
     */
    private void doHash(int sourceType) {
        this.lastFunction = sourceType;
        String type = (String) this.component.selectEncryptHashType.getModel().getSelectedItem();
        if (type == null || type.trim().isEmpty()) {
            NotificationUtil.error(MessageUtil.getMessage("encrypt.hash.tip.digest.is.null"));
            return;
        }
        String keyStr = this.component.textEncryptHashKey.getText();
        byte[] key = null;
        if (keyStr != null && !keyStr.trim().isEmpty()) {
            try {
                key = Base64.getDecoder().decode(keyStr);
            } catch (Exception ex) {
                try {
                    key = HexUtil.decodeHex(keyStr);
                } catch (Exception ex2) {
                    NotificationUtil.error(MessageUtil.getMessage("encrypt.hash.tip.hmac.key.can.not.parse"));
                    return;
                }
            }
        }
        byte[] finalKey = key;
        ProgressManager.getInstance().run(new Task.Backgroundable(this.component.getCurrentProject(), MessageUtil.getMessage("encrypt.hash.tip.running")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                if (0 == sourceType) {
                    // hash file
                    component.textareaEncryptHashResult.setText("");
                    AtomicLong pgInFinished = new AtomicLong(0);
                    AtomicLong pgInMax = new AtomicLong(toSelects.length);
                    for (VirtualFile file : toSelects) {
                        _doHashFromFile(file, type, finalKey, progressIndicator, pgInFinished, pgInMax);
                        progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                    }
                } else if (1 == sourceType) {
                    // hash text
                    if (component.checkEncryptHashLine.isSelected()) {
                        String source = component.textareaEncryptHashText.getText();
                        String[] split = source.replace("\r", "").split("\n");
                        component.textareaEncryptHashResult.setText("");
                        AtomicLong pgInFinished = new AtomicLong(0);
                        AtomicLong pgInMax = new AtomicLong(split.length);
                        for (String line : split) {
                            try {
                                DigestAssertResult eachResult = hash(type, finalKey, line, false, getAssertStr(null));
                                component.textareaEncryptHashResult.append(
                                        (eachResult.getIsMatch() == null ? "" :
                                                (eachResult.getIsMatch() ?
                                                        (MessageUtil.getMessage("encrypt.hash.tip.compare.match") + " ") :
                                                        (MessageUtil.getMessage("encrypt.hash.tip.compare.not.match") + " "))
                                        ) + eachResult.getResult());
                                component.textareaEncryptHashResult.append("\n");
                            } catch (Exception ex) {
                                component.textareaEncryptHashResult.append(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
                                component.textareaEncryptHashResult.append("\n");
                            } finally {
                                progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                            }
                        }
                    } else {
                        String source = component.textareaEncryptHashText.getText();
                        try {
                            DigestAssertResult eachResult = hash(type, finalKey, source, false, getAssertStr(null));
                            component.textareaEncryptHashResult.setText(
                                    (eachResult.getIsMatch() == null ? "" :
                                            (eachResult.getIsMatch() ?
                                                    (MessageUtil.getMessage("encrypt.hash.tip.compare.match") + " ") :
                                                    (MessageUtil.getMessage("encrypt.hash.tip.compare.not.match") + " "))
                                    ) + eachResult.getResult());
                        } catch (Exception ex) {
                            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                        }
                    }
                }
            }
        });
    }

    private void _doHashFromFile(VirtualFile file, String type, byte[] key, ProgressIndicator progressIndicator, AtomicLong pgInFinished, AtomicLong pgInMax) {
        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            if (children != null) {
                pgInMax.addAndGet(children.length);
                for (VirtualFile child : children) {
                    _doHashFromFile(child, type, key, progressIndicator, pgInFinished, pgInMax);
                    progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                }
            }
        } else {
            try {
                // 不符合路径正则条件的过滤掉，不进行计算和输出
                String filterRegex = this.component.textEncryptHashPathNameFilter.getText();
                if (filterRegex != null && !filterRegex.trim().isEmpty()) {
                    Matcher matcher = Pattern.compile(filterRegex).matcher(file.getPresentableUrl());
                    if (filterRegex.startsWith("^") || filterRegex.endsWith("$")) {
                        if (!matcher.matches()) {
                            return;
                        }
                    } else {
                        if (!matcher.find()) {
                            return;
                        }
                    }
                }
                DigestAssert assertStr = this.getAssertStr(file);
                DigestAssertResult eachResult = this.hash(type, key, file.getPath(), true, assertStr);
                this.component.textareaEncryptHashResult.append("Name: " + file.getPresentableUrl());
                this.component.textareaEncryptHashResult.append("\n");
                String _type = type;
                if (assertStr != null && assertStr.getForceDigest() != null) {
                    _type = assertStr.getForceDigest();
                }
                this.component.textareaEncryptHashResult.append(_type + "-Digest: " +
                        (eachResult.getIsMatch() == null ? "" :
                                (eachResult.getIsMatch() ?
                                        (MessageUtil.getMessage("encrypt.hash.tip.compare.match") + " ") :
                                        (MessageUtil.getMessage("encrypt.hash.tip.compare.not.match") + " "))
                        ) + eachResult.getResult());
                this.component.textareaEncryptHashResult.append("\n");
                this.component.textareaEncryptHashResult.append("\n");
            } catch (Exception ex) {
                this.component.textareaEncryptHashResult.append("Name: " + file.getPresentableUrl());
                this.component.textareaEncryptHashResult.append("\n");
                this.component.textareaEncryptHashResult.append(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
                this.component.textareaEncryptHashResult.append("\n");
                this.component.textareaEncryptHashResult.append("\n");
            }
        }
    }

    private void generateHMacKey(ActionEvent e) {
        String type = (String) this.component.selectEncryptHashType.getModel().getSelectedItem();
        if (type == null || !type.toUpperCase().startsWith("HMAC")) {
            NotificationUtil.error(MessageUtil.getMessage("encrypt.hash.tip.hmac.used.only"));
            return;
        }
        byte[] key = SecureUtil.generateKey(type).getEncoded();
        key = Base64.getEncoder().encode(key);
        try {
            this.component.textEncryptHashKey.setText(new String(key, getCharset()));
        } catch (UnsupportedEncodingException ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    private Charset getCharset() throws UnsupportedEncodingException {
        int encodingModelIndex = this.component.selectEncryptHashEncoding.getSelectedIndex();
        Object selectedObjects = this.component.selectEncryptHashEncoding.getModel().getSelectedItem();
        if (encodingModelIndex == 0) {
            return Charset.defaultCharset();
        } else {
            return Charset.forName((String) selectedObjects);
        }
    }

    private DigestAssertResult hash(String type, byte[] key, String source, boolean sourceIsFile, DigestAssert assertStr) throws UnsupportedEncodingException {
        if (assertStr != null && assertStr.getForceDigest() != null) {
            type = assertStr.getForceDigest();
        }
        String outputType = (String) this.component.selectEncryptHashOutputType.getModel().getSelectedItem();
        if (assertStr != null && assertStr.getForceOutputType() != null) {
            outputType = assertStr.getForceOutputType();
        }
        String resultHex;
        if (type.toUpperCase().startsWith("HMAC")) {
            HMac digester = new HMac(type, key);
            resultHex = sourceIsFile ? digester.digestHex(new File(source)) : digester.digestHex(source);
        } else {
            Digester digester = DigestUtil.digester(type);
            resultHex = sourceIsFile ? digester.digestHex(new File(source)) : digester.digestHex(source);
        }
        if ("Base64".equalsIgnoreCase(outputType)) {
            resultHex = new String(Base64.getEncoder().encode(HexUtil.decodeHex(resultHex)), getCharset());
        }
        if (assertStr != null && !assertStr.getAssertStr().trim().isEmpty()) {
            if (assertStr.getAssertStr().equalsIgnoreCase(resultHex)) {
                return new DigestAssertResult(resultHex, true);
            }
            return new DigestAssertResult(resultHex, false);
        } else {
            return new DigestAssertResult(resultHex, null);
        }
    }

    @Data
    @AllArgsConstructor
    private static class DigestAssert {
        private String assertStr;
        private String forceDigest;
        private String forceOutputType;
    }

    @Data
    @AllArgsConstructor
    private static class DigestAssertResult {
        private String result;
        private Boolean isMatch;
    }

}
