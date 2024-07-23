package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.AsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

/**
 * 非对称加解密
 *
 * @author IceRain
 * @since 2022/01/26
 */
public class MainFormEncryptAsymmetricFunction {

    private final ToolWindowEncrypt component;
    private VirtualFile toSelectPublicKey = null;
    private VirtualFile toSelectPrivateKey = null;
    private int nowKeyPairGenLength = -1;

    private final Project project;

    public MainFormEncryptAsymmetricFunction(Project project, ToolWindowEncrypt component) {
        this.project = project;
        this.component = component;
        this.component.buttonEncryptAsymmetricSwitchSm2KeyType.addActionListener(this::switchSm2KeyType);
        this.component.fileEncryptAsymmetricPublicKey.addActionListener(this::selectPublicKey);
        this.component.fileEncryptAsymmetricPrivateKey.addActionListener(this::selectPrivateKey);
        this.component.buttonEncryptAsymmetricGenerateKey.addActionListener(this::generateKey);
        this.component.buttonEncryptAsymmetricClean.addActionListener(e -> this.clean());
        this.component.buttonEncryptAsymmetricEncryptWithPublicKey.addActionListener(e -> {
            String result;
            try {
                result = this.encryptWithPublicKey(this.component.textareaEncryptAsymmetricDecrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.component.textareaEncryptAsymmetricEncrypted.setText(result);
        });
        this.component.buttonEncryptAsymmetricDecryptWithPrivateKey.addActionListener(e -> {
            String result;
            try {
                result = this.decryptWithPrivateKey(this.component.textareaEncryptAsymmetricEncrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.component.textareaEncryptAsymmetricDecrypted.setText(result);
        });
        this.component.buttonEncryptAsymmetricEncryptWithPrivateKey.addActionListener(e -> {
            String result;
            try {
                result = this.encryptWithPrivateKey(this.component.textareaEncryptAsymmetricDecrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.component.textareaEncryptAsymmetricEncrypted.setText(result);
        });
        this.component.buttonEncryptAsymmetricDecryptWithPublicKey.addActionListener(e -> {
            String result;
            try {
                result = this.decryptWithPublicKey(this.component.textareaEncryptAsymmetricEncrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.component.textareaEncryptAsymmetricDecrypted.setText(result);
        });
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.component.scrollEncryptAsymmetricDecrypted, this.component.scrollEncryptAsymmetricEncrypted);
        this.component.scrollEncryptAsymmetricDecrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptAsymmetricDecrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptAsymmetricEncrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollEncryptAsymmetricEncrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void clean() {
        this.component.textareaEncryptAsymmetricEncrypted.setText("");
        this.component.textareaEncryptAsymmetricDecrypted.setText("");
    }

    private void switchSm2KeyType(ActionEvent e) {
        String algorithm = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        if ("SM2".equals(algorithm)) {
            // 0: Base64 -> Hex; 1: Hex -> Base64
            int mode = -1;
            byte[] pubKey = null;
            byte[] privKey = null;
            String publicKey = this.component.textEncryptAsymmetricPublicKey.getText();
            if (publicKey != null && !publicKey.trim().isEmpty()) {
                try {
                    pubKey = HexUtil.decodeHex(publicKey);
                    mode = 1;
                } catch (Exception ignored) {
                    pubKey = Base64.getDecoder().decode(publicKey);
                    mode = 0;
                }
            }
            String privateKey = this.component.textEncryptAsymmetricPrivateKey.getText();
            if (privateKey != null && !privateKey.trim().isEmpty()) {
                try {
                    privKey = HexUtil.decodeHex(privateKey);
                    mode = mode == -1 ? 1 : mode;
                } catch (Exception ignored) {
                    privKey = Base64.getDecoder().decode(privateKey);
                    mode = mode == -1 ? 0 : mode;
                }
            }
            if (mode == 0) {
                // to Hex
                if (publicKey != null && !publicKey.trim().isEmpty() && pubKey != null) {
                    SM2 sm2 = new SM2(null, pubKey);
                    byte[] q = sm2.getQ(false);
                    publicKey = HexUtil.encodeHexStr(q);
                }
                if (privateKey != null && !privateKey.trim().isEmpty() && privKey != null) {
                    SM2 sm2 = new SM2(privKey, null);
                    privateKey = sm2.getDHex();
                }
            } else if (mode == 1) {
                // TODO missing ECDomainParameters(curve, G, n, h, seed) can't be re-create
//                // to Base64
//                if (publicKey != null && !publicKey.trim().isEmpty() && pubKey != null) {
//                    SM2 sm2 = new SM2(null, pubKey);
//                    publicKey = sm2.getPublicKeyBase64();
//                }
//                if (privateKey != null && !privateKey.trim().isEmpty() && privKey != null) {
//                    SM2 sm2 = new SM2(privKey, null);
//                    privateKey = sm2.getPrivateKeyBase64();
//                }
            }
            this.component.textEncryptAsymmetricPublicKey.setText(publicKey);
            this.component.textEncryptAsymmetricPrivateKey.setText(privateKey);
        }
    }

    private void selectPublicKey(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
        this.toSelectPublicKey = FileChooser.chooseFile(descriptor, null, this.toSelectPublicKey);
        if (this.toSelectPublicKey == null) {
            return;
        }
        String publicKey;
        try (InputStream fileIs = this.toSelectPublicKey.getInputStream()) {
            // KeyFile not contain non-ASCII chars, so this use any charsets also can read file to use.
            publicKey = new String(fileIs.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            return;
        }
        this.component.textEncryptAsymmetricPublicKey.setText(publicKey);
    }

    private void selectPrivateKey(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
        this.toSelectPrivateKey = FileChooser.chooseFile(descriptor, null, this.toSelectPrivateKey);
        if (this.toSelectPrivateKey == null) {
            return;
        }
        String privateKey;
        try (InputStream fileIs = this.toSelectPrivateKey.getInputStream()) {
            // KeyFile not contain non-ASCII chars, so this use any charsets also can read file to use.
            privateKey = new String(fileIs.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            return;
        }
        this.component.textEncryptAsymmetricPrivateKey.setText(privateKey);
    }

    private void generateKey(ActionEvent e) {
        this.component.buttonEncryptAsymmetricGenerateKey.setEnabled(false);
        String algorithm = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        if (!"SM2".equals(algorithm)) {
            if ((ActionEvent.SHIFT_MASK & e.getModifiers()) != 0) {
                nowKeyPairGenLength = switch (nowKeyPairGenLength) {
                    case 1024 -> 2048;
                    case 2048 -> 4096;
                    case 4096, 8192 -> 8192;
                    default -> 1024;
                };
            } else if ((ActionEvent.CTRL_MASK & e.getModifiers()) != 0) {
                nowKeyPairGenLength = switch (nowKeyPairGenLength) {
                    case 8192 -> 4096;
                    case 4096 -> 2048;
                    default -> 1024;
                };
            } else if (nowKeyPairGenLength == -1) {
                nowKeyPairGenLength = 1024;
            }
        }
        this.component.buttonEncryptAsymmetricGenerateKey.setText(MessageUtil.getMessage("encrypt.asymmetric.button.generate.key.title") + " (" + ("SM2".equals(algorithm) ? 256 : nowKeyPairGenLength) + ")");
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("encrypt.asymmetric.tip.generate")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                KeyPair keyPair = null;
                try {
                    keyPair = SecureUtil.generateKeyPair(algorithm, "SM2".equals(algorithm) ? 256 : nowKeyPairGenLength);
                } catch (Exception ex) {
                    NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                } finally {
                    try {
                        progressIndicator.checkCanceled();
                    } catch (ProcessCanceledException ex) {
                        keyPair = null;
                    } finally {
                        setTextAfterGenerateKeyPair(keyPair);
                    }
                }
            }
        });
    }

    private void setTextAfterGenerateKeyPair(KeyPair keyPair) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // EDT
            try {
                if (keyPair != null) {
                    this.component.textEncryptAsymmetricPublicKey.setText(new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()), getCharset()));
                    this.component.textEncryptAsymmetricPrivateKey.setText(new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()), getCharset()));
                }
            } catch (UnsupportedEncodingException ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            } finally {
                this.component.buttonEncryptAsymmetricGenerateKey.setEnabled(true);
            }
        });
    }

    private Charset getCharset() throws UnsupportedEncodingException {
        int encodingModelIndex = this.component.selectEncryptAsymmetricEncoding.getSelectedIndex();
        Object selectedObjects = this.component.selectEncryptAsymmetricEncoding.getModel().getSelectedItem();
        if (encodingModelIndex == 0) {
            return Charset.defaultCharset();
        } else {
            return Charset.forName((String) selectedObjects);
        }
    }

    private String encryptWithPublicKey(String source) throws UnsupportedEncodingException {
        String publicKey = this.component.textEncryptAsymmetricPublicKey.getText();
        if (publicKey == null || publicKey.trim().isEmpty() || source == null || source.trim().isEmpty()) {
            return "";
        }
        String type = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, null, publicKey);
        byte[] encrypt = crypto.encrypt(source.getBytes(getCharset()), KeyType.PublicKey);
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, getCharset());
    }

    private String decryptWithPrivateKey(String source) throws UnsupportedEncodingException {
        String privateKey = this.component.textEncryptAsymmetricPrivateKey.getText();
        if (privateKey == null || privateKey.trim().isEmpty() || source == null || source.trim().isEmpty()) {
            return "";
        }
        String type = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, privateKey, null);
        byte[] decode = Base64.getDecoder().decode(source.getBytes(getCharset()));
        byte[] decrypt = crypto.decrypt(decode, KeyType.PrivateKey);
        return new String(decrypt, getCharset());
    }

    private String encryptWithPrivateKey(String source) throws UnsupportedEncodingException {
        String privateKey = this.component.textEncryptAsymmetricPrivateKey.getText();
        if (privateKey == null || privateKey.trim().isEmpty() || source == null || source.trim().isEmpty()) {
            return "";
        }
        String type = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, privateKey, null);
        byte[] encrypt = crypto.encrypt(source.getBytes(getCharset()), KeyType.PrivateKey);
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, getCharset());
    }

    private String decryptWithPublicKey(String source) throws UnsupportedEncodingException {
        String publicKey = this.component.textEncryptAsymmetricPublicKey.getText();
        if (publicKey == null || publicKey.trim().isEmpty() || source == null || source.trim().isEmpty()) {
            return "";
        }
        String type = (String) this.component.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, null, publicKey);
        byte[] decode = Base64.getDecoder().decode(source.getBytes(getCharset()));
        byte[] decrypt = crypto.decrypt(decode, KeyType.PublicKey);
        return new String(decrypt, getCharset());
    }

}
