package com.gmail.litalways.toolset.gui;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.AsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

/**
 * 对称加解密
 *
 * @author IceRain
 * @since 2022/01/26
 */
public class MainFormEncryptAsymmetricFunction {

    private final MainForm mainForm;
    private VirtualFile toSelectPublicKey = null;
    private VirtualFile toSelectPrivateKey = null;

    public MainFormEncryptAsymmetricFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.fileEncryptAsymmetricPublicKey.addActionListener(this::selectPublicKey);
        this.mainForm.fileEncryptAsymmetricPrivateKey.addActionListener(this::selectPrivateKey);
        this.mainForm.buttonEncryptAsymmetricGenerateKey.addActionListener(this::generateKey);
        this.mainForm.buttonEncryptAsymmetricClean.addActionListener(e -> this.clean());
        this.mainForm.buttonEncryptAsymmetricEncryptWithPublicKey.addActionListener(e -> {
            String result;
            try {
                result = this.encryptWithPublicKey(this.mainForm.textareaEncryptAsymmetricDecrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.mainForm.textareaEncryptAsymmetricEncrypted.setText(result);
        });
        this.mainForm.buttonEncryptAsymmetricDecryptWithPrivateKey.addActionListener(e -> {
            String result;
            try {
                result = this.decryptWithPrivateKey(this.mainForm.textareaEncryptAsymmetricEncrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.mainForm.textareaEncryptAsymmetricDecrypted.setText(result);
        });
        this.mainForm.buttonEncryptAsymmetricEncryptWithPrivateKey.addActionListener(e -> {
            String result;
            try {
                result = this.encryptWithPrivateKey(this.mainForm.textareaEncryptAsymmetricDecrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.mainForm.textareaEncryptAsymmetricEncrypted.setText(result);
        });
        this.mainForm.buttonEncryptAsymmetricDecryptWithPublicKey.addActionListener(e -> {
            String result;
            try {
                result = this.decryptWithPublicKey(this.mainForm.textareaEncryptAsymmetricEncrypted.getText());
            } catch (Exception ex) {
                result = ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            }
            this.mainForm.textareaEncryptAsymmetricDecrypted.setText(result);
        });
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollEncryptAsymmetricDecrypted, this.mainForm.scrollEncryptAsymmetricEncrypted);
        this.mainForm.scrollEncryptAsymmetricDecrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptAsymmetricDecrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptAsymmetricEncrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptAsymmetricEncrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void clean() {
        this.mainForm.textareaEncryptAsymmetricEncrypted.setText("");
        this.mainForm.textareaEncryptAsymmetricDecrypted.setText("");
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
        this.mainForm.textEncryptAsymmetricPublicKey.setText(publicKey);
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
        this.mainForm.textEncryptAsymmetricPrivateKey.setText(privateKey);
    }

    private void generateKey(ActionEvent e) {
        try {
            KeyPair keyPair = SecureUtil.generateKeyPair((String) this.mainForm.selectEncryptAsymmetricType.getModel().getSelectedItem());
            this.mainForm.textEncryptAsymmetricPublicKey.setText(new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()), getCharset()));
            this.mainForm.textEncryptAsymmetricPrivateKey.setText(new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()), getCharset()));
        } catch (UnsupportedEncodingException ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    private String getCharset() {
        int encodingModelIndex = this.mainForm.selectEncryptAsymmetricEncoding.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectEncryptAsymmetricEncoding.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private String encryptWithPublicKey(String source) throws UnsupportedEncodingException {
        String publicKey = this.mainForm.textEncryptAsymmetricPublicKey.getText();
        if (publicKey == null || publicKey.trim().length() == 0 || source == null || source.trim().length() == 0) {
            return "";
        }
        String type = (String) this.mainForm.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, null, publicKey);
        byte[] encrypt = crypto.encrypt(source.getBytes(getCharset()), KeyType.PublicKey);
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, getCharset());
    }

    private String decryptWithPrivateKey(String source) throws UnsupportedEncodingException {
        String privateKey = this.mainForm.textEncryptAsymmetricPrivateKey.getText();
        if (privateKey == null || privateKey.trim().length() == 0 || source == null || source.trim().length() == 0) {
            return "";
        }
        String type = (String) this.mainForm.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, privateKey, null);
        byte[] decode = Base64.getDecoder().decode(source.getBytes(getCharset()));
        byte[] decrypt = crypto.decrypt(decode, KeyType.PrivateKey);
        return new String(decrypt, getCharset());
    }

    private String encryptWithPrivateKey(String source) throws UnsupportedEncodingException {
        String privateKey = this.mainForm.textEncryptAsymmetricPrivateKey.getText();
        if (privateKey == null || privateKey.trim().length() == 0 || source == null || source.trim().length() == 0) {
            return "";
        }
        String type = (String) this.mainForm.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, privateKey, null);
        byte[] encrypt = crypto.encrypt(source.getBytes(getCharset()), KeyType.PrivateKey);
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, getCharset());
    }

    private String decryptWithPublicKey(String source) throws UnsupportedEncodingException {
        String publicKey = this.mainForm.textEncryptAsymmetricPublicKey.getText();
        if (publicKey == null || publicKey.trim().length() == 0 || source == null || source.trim().length() == 0) {
            return "";
        }
        String type = (String) this.mainForm.selectEncryptAsymmetricType.getModel().getSelectedItem();
        AsymmetricCrypto crypto = new AsymmetricCrypto(type, null, publicKey);
        byte[] decode = Base64.getDecoder().decode(source.getBytes(getCharset()));
        byte[] decrypt = crypto.decrypt(decode, KeyType.PublicKey);
        return new String(decrypt, getCharset());
    }

}
