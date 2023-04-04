package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.HMac;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author IceRain
 * @since 2022/01/27
 */
public class MainFormEncryptHashFunction {

    private final MainForm mainForm;
    private VirtualFile[] toSelects = new VirtualFile[0];
    private VirtualFile[] toSelectAssets = new VirtualFile[0];
    private Integer lastFunction = null;

    public MainFormEncryptHashFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonEncryptHashGenerateKey.addActionListener(this::generateHMacKey);
        this.mainForm.buttonEncryptHashClean.addActionListener(e -> this.clean());
        this.mainForm.fileEncryptHashFile.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, true);
            this.toSelects = FileChooser.chooseFiles(descriptor, null, this.toSelects.length == 0 ? null : this.toSelects[this.toSelects.length - 1]);
            if (this.toSelects.length == 0) {
                this.mainForm.fileEncryptHashFile.setText("");
                return;
            }
            this.mainForm.fileEncryptHashFile.setText(this.toSelects[this.toSelects.length - 1].getPresentableUrl() + (this.toSelects.length == 1 ? "" : (" ... and " + (this.toSelects.length - 1) + " files.")));
            // Calc size and tips
            for (VirtualFile file : this.toSelects) {
                if (file.getLength() >= 524288000) {
                    NotificationUtil.warning(MessageUtil.getMessage("convert.hash.large.file"), "\"certutil -hashfile src.bin MD5 >> hash.txt\"");
                }
            }
            this.doHash(0);
        });
        this.mainForm.fileEncryptHashAsserts.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, true);
            this.toSelectAssets = FileChooser.chooseFiles(descriptor, null, this.toSelectAssets.length == 0 ? null : this.toSelectAssets[this.toSelectAssets.length - 1]);
            if (this.toSelectAssets.length == 0) {
                this.mainForm.fileEncryptHashAsserts.setText("");
                return;
            }
            this.mainForm.fileEncryptHashAsserts.setText(this.toSelectAssets[this.toSelectAssets.length - 1].getPresentableUrl() + (this.toSelectAssets.length == 1 ? "" : (" ... and " + (this.toSelectAssets.length - 1) + " files.")));
            if (this.toSelectAssets.length != this.toSelects.length) {
                NotificationUtil.warning(MessageUtil.getMessage("convert.hash.compare.files.count.not.equal.bin.files"));
            }
            this.mainForm.textareaEncryptHashText.setText("");
            this.doHash(0);
        });
        this.mainForm.textareaEncryptHashText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doHash(1);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doHash(1);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.mainForm.buttonEncryptHashFile.addActionListener(e -> this.doHash(0));
        this.mainForm.buttonEncryptHashText.addActionListener(e -> this.doHash(1));
        this.mainForm.selectEncryptHashEncoding.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        this.mainForm.selectEncryptHashType.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        this.mainForm.selectEncryptHashOutputType.addActionListener(e -> {
            if (this.lastFunction != null) {
                this.doHash(this.lastFunction);
            }
        });
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollEncryptHashText, this.mainForm.scrollEncryptHashResult);
        this.mainForm.scrollEncryptHashText.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptHashText.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptHashResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptHashResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void clean() {
        this.mainForm.textareaEncryptHashText.setText("");
        this.mainForm.textareaEncryptHashResult.setText("");
        this.mainForm.fileEncryptHashAsserts.setText("");
        this.toSelectAssets = new VirtualFile[0];
        this.mainForm.textareaEncryptHashText.setText("");
        this.mainForm.textEncryptHashKey.setText("");
    }

    private DigestAssert getAssertStr(VirtualFile source) {
        String assertStr = this.mainForm.textEncryptHashAssert.getText();
        if (assertStr != null && assertStr.trim().length() > 0) {
            return new DigestAssert(assertStr, null, null);
        }
        if (source == null || this.toSelectAssets.length == 0) {
            return null;
        }
        if (this.toSelectAssets.length == 1 && "MANIFEST.MF".equals(this.toSelectAssets[0].getName())) {
            String manifest;
            try (InputStream fileIs = this.toSelectAssets[0].getInputStream()) {
                manifest = new String(fileIs.readAllBytes(), getCharset());
            } catch (Exception ex) {
                return null;
            }
            String[] arr = manifest.replace("\r", "").split("\n");
            boolean matchLine = false;
            for (String line : arr) {
                if (matchLine) {
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
                        // boo.txt <=> foo.txt
                        if (source.getName().equals(fileNameWithSuffix)) {
                            matchLine = true;
                        }
                    }
                }
            }
            return null;
        } else {
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

    private void doHash(int sourceType) {
        this.lastFunction = sourceType;
        String type = (String) this.mainForm.selectEncryptHashType.getModel().getSelectedItem();
        if (type == null || type.trim().length() == 0) {
            NotificationUtil.error(MessageUtil.getMessage("convert.hash.digest.is.null"));
            return;
        }
        String keyStr = this.mainForm.textEncryptHashKey.getText();
        byte[] key = null;
        if (keyStr != null && keyStr.trim().length() > 0) {
            try {
                key = Base64.getDecoder().decode(keyStr);
            } catch (Exception ex) {
                try {
                    key = HexUtil.decodeHex(keyStr);
                } catch (Exception ex2) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.hash.hmac.key.can.not.parse"));
                    return;
                }
            }
        }
        if (0 == sourceType) {
            // hash file
            this.mainForm.textareaEncryptHashResult.setText("");
            for (VirtualFile file : this.toSelects) {
                try {
                    DigestAssert assertStr = this.getAssertStr(file);
                    DigestAssertResult eachResult = this.hash(type, key, file.getPath(), true, assertStr);
                    this.mainForm.textareaEncryptHashResult.append("Name: " + file.getPresentableUrl());
                    this.mainForm.textareaEncryptHashResult.append("\n");
                    String _type = type;
                    if (assertStr != null && assertStr.getForceDigest() != null) {
                        _type = assertStr.getForceDigest();
                    }
                    this.mainForm.textareaEncryptHashResult.append(_type + "-Digest: " +
                            (eachResult.getIsMatch() == null ? "" :
                                    (eachResult.getIsMatch() ?
                                            (MessageUtil.getMessage("convert.hash.compare.match") + " ") :
                                            (MessageUtil.getMessage("convert.hash.compare.not.match") + " "))
                            ) + eachResult.getResult());
                    this.mainForm.textareaEncryptHashResult.append("\n");
                    this.mainForm.textareaEncryptHashResult.append("\n");
                } catch (Exception ex) {
                    this.mainForm.textareaEncryptHashResult.append("Name: " + file.getPresentableUrl());
                    this.mainForm.textareaEncryptHashResult.append("\n");
                    this.mainForm.textareaEncryptHashResult.append(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
                    this.mainForm.textareaEncryptHashResult.append("\n");
                    this.mainForm.textareaEncryptHashResult.append("\n");
                }
            }
        } else if (1 == sourceType) {
            // hash text
            if (this.mainForm.checkEncryptHashLine.isSelected()) {
                String source = this.mainForm.textareaEncryptHashText.getText();
                String[] split = source.replace("\r", "").split("\n");
                this.mainForm.textareaEncryptHashResult.setText("");
                for (String line : split) {
                    try {
                        DigestAssertResult eachResult = this.hash(type, key, line, false, this.getAssertStr(null));
                        this.mainForm.textareaEncryptHashResult.append(
                                (eachResult.getIsMatch() == null ? "" :
                                        (eachResult.getIsMatch() ?
                                                (MessageUtil.getMessage("convert.hash.compare.match") + " ") :
                                                (MessageUtil.getMessage("convert.hash.compare.not.match") + " "))
                                ) + eachResult.getResult());
                        this.mainForm.textareaEncryptHashResult.append("\n");
                    } catch (Exception ex) {
                        this.mainForm.textareaEncryptHashResult.append(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
                        this.mainForm.textareaEncryptHashResult.append("\n");
                    }
                }
            } else {
                String source = this.mainForm.textareaEncryptHashText.getText();
                try {
                    DigestAssertResult eachResult = this.hash(type, key, source, false, this.getAssertStr(null));
                    this.mainForm.textareaEncryptHashResult.setText(
                            (eachResult.getIsMatch() == null ? "" :
                                    (eachResult.getIsMatch() ?
                                            (MessageUtil.getMessage("convert.hash.compare.match") + " ") :
                                            (MessageUtil.getMessage("convert.hash.compare.not.match") + " "))
                            ) + eachResult.getResult());
                } catch (Exception ex) {
                    NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                    return;
                }
            }
        }
    }

    private void generateHMacKey(ActionEvent e) {
        String type = (String) this.mainForm.selectEncryptHashType.getModel().getSelectedItem();
        if (type == null || !type.toUpperCase().startsWith("HMAC")) {
            NotificationUtil.error(MessageUtil.getMessage("convert.hash.hmac.used.only"));
            return;
        }
        byte[] key = SecureUtil.generateKey(type).getEncoded();
        key = Base64.getEncoder().encode(key);
        try {
            this.mainForm.textEncryptHashKey.setText(new String(key, getCharset()));
        } catch (UnsupportedEncodingException ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    private String getCharset() {
        int encodingModelIndex = this.mainForm.selectEncryptHashEncoding.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectEncryptHashEncoding.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private DigestAssertResult hash(String type, byte[] key, String source, boolean sourceIsFile, DigestAssert assertStr) throws UnsupportedEncodingException {
        if (assertStr != null && assertStr.getForceDigest() != null) {
            type = assertStr.getForceDigest();
        }
        String outputType = (String) this.mainForm.selectEncryptHashOutputType.getModel().getSelectedItem();
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
        if (assertStr != null && assertStr.getAssertStr().trim().length() > 0) {
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
