package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 非对称加解密
 *
 * @author IceRain
 * @since 2022/01/26
 */
public class MainFormEncryptSymmetricFunction {

    private final MainForm mainForm;

    public MainFormEncryptSymmetricFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonEncryptSymmetricEncrypt.addActionListener(this::encrypt);
        this.mainForm.buttonEncryptSymmetricDecrypt.addActionListener(this::decrypt);
        this.mainForm.buttonEncryptSymmetricClean.addActionListener(e -> this.clean());
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollEncryptSymmetricDecrypted, this.mainForm.scrollEncryptSymmetricEncrypted);
        this.mainForm.scrollEncryptSymmetricDecrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptSymmetricDecrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptSymmetricEncrypted.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollEncryptSymmetricEncrypted.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void clean() {
        this.mainForm.textareaEncryptSymmetricEncrypted.setText("");
        this.mainForm.textareaEncryptSymmetricDecrypted.setText("");
    }

    private void encrypt(ActionEvent e) {
        try {
            String source = this.mainForm.textareaEncryptSymmetricDecrypted.getText();
            String type = (String) this.mainForm.selectEncryptSymmetricType.getModel().getSelectedItem();
            String mode = (String) this.mainForm.selectEncryptSymmetricMode.getModel().getSelectedItem();
            int padding = this.mainForm.selectEncryptSymmetricPadding.getSelectedIndex();
            String key = this.mainForm.textEncryptSymmetricKey.getText();
            String iv = this.mainForm.textEncryptSymmetricIV.getText();
            String salt = this.mainForm.textEncryptSymmetricSalt.getText();
            String outputType = (String) this.mainForm.selectEncryptSymmetricOutputType.getModel().getSelectedItem();
            String dest = func(true, type, mode, padding, key, iv, salt, source, outputType);
            this.mainForm.textareaEncryptSymmetricEncrypted.setText(dest);
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    private void decrypt(ActionEvent e) {
        try {
            String source = this.mainForm.textareaEncryptSymmetricEncrypted.getText();
            String type = (String) this.mainForm.selectEncryptSymmetricType.getModel().getSelectedItem();
            String mode = (String) this.mainForm.selectEncryptSymmetricMode.getModel().getSelectedItem();
            int padding = this.mainForm.selectEncryptSymmetricPadding.getSelectedIndex();
            String key = this.mainForm.textEncryptSymmetricKey.getText();
            String iv = this.mainForm.textEncryptSymmetricIV.getText();
            String salt = this.mainForm.textEncryptSymmetricSalt.getText();
            String outputType = (String) this.mainForm.selectEncryptSymmetricOutputType.getModel().getSelectedItem();
            String dest = func(false, type, mode, padding, key, iv, salt, source, outputType);
            this.mainForm.textareaEncryptSymmetricDecrypted.setText(dest);
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    private String getCharset() {
        int encodingModelIndex = this.mainForm.selectEncryptSymmetricEncoding.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectEncryptSymmetricEncoding.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private String func(boolean isEncrypt, String type, String modeStr, int paddingIndex, String keyStr, String ivStr, String saltStr, String sourceStr, String outputType) throws UnsupportedEncodingException {
        Mode mode;
        if ("CBC".equalsIgnoreCase(modeStr)) {
            mode = Mode.CBC;
        } else if ("CFB".equalsIgnoreCase(modeStr)) {
            mode = Mode.CFB;
        } else if ("CTR".equalsIgnoreCase(modeStr)) {
            mode = Mode.CTR;
        } else if ("CTS".equalsIgnoreCase(modeStr)) {
            mode = Mode.CTS;
        } else if ("ECB".equalsIgnoreCase(modeStr)) {
            mode = Mode.ECB;
        } else if ("OFB".equalsIgnoreCase(modeStr)) {
            mode = Mode.OFB;
        } else if ("PCBC".equalsIgnoreCase(modeStr)) {
            mode = Mode.PCBC;
        } else {
            throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.mode.not.support", modeStr));
        }
        Padding padding = switch (paddingIndex) {
            case 0 -> Padding.NoPadding;
            case 1 -> Padding.ZeroPadding;
            case 2 -> Padding.ISO10126Padding;
            case 3 -> Padding.OAEPPadding;
            case 4 -> Padding.PKCS1Padding;
            case 5 -> Padding.PKCS5Padding;
            case 6 -> Padding.SSL3Padding;
            default ->
                    throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.padding.not.support",
                            this.mainForm.selectEncryptSymmetricPadding.getModel().getSelectedItem()));
        };
        byte[] key = keyStr.getBytes(getCharset());
        byte[] iv = null;
        if (ivStr != null && ivStr.trim().length() > 0) {
            iv = ivStr.getBytes(getCharset());
        }
        SymmetricCrypto crypto;
        boolean pbe = false;
        boolean jasypt = false;
        if ("PBEWithMD5AndDES".equalsIgnoreCase(type)) {
            pbe = true;
            crypto = new SymmetricCrypto("PBEWithMD5AndDES", key);
        } else if ("JasyptDefault".equalsIgnoreCase(type)) {
            pbe = true;
            jasypt = true;
            crypto = new SymmetricCrypto("PBEWithMD5AndDES", key);
        } else {
            crypto = new SymmetricCrypto(type + "/" + mode + "/" + padding, key);
        }
        if (!pbe && iv != null) {
            crypto.setIv(iv);
        }
        if (isEncrypt) {
            byte[] source = sourceStr.getBytes(getCharset());
            if (pbe) {
                int keyObtentionIterations = 1000;
                if (iv == null) {
                    iv = new byte[0];
                }
                if (jasypt) {
                    int saltSizeBytes = 8;
                    SecureRandom random;
                    try {
                        random = SecureRandom.getInstance("SHA1PRNG");
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.jasypt.secure.random.lose.sha1prng"));
                    }
                    byte[] salt = new byte[saltSizeBytes];
                    random.nextBytes(salt);
                    PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, keyObtentionIterations, new IvParameterSpec(iv));
                    crypto.setParams(parameterSpec);
                    byte[] encrypt = crypto.encrypt(source);
                    byte[] encryptedBytes = new byte[iv.length + encrypt.length];
                    System.arraycopy(iv, 0, encryptedBytes, 0, iv.length);
                    System.arraycopy(encrypt, 0, encryptedBytes, iv.length, encrypt.length);
                    byte[] encryptedBytes2 = new byte[salt.length + encryptedBytes.length];
                    System.arraycopy(salt, 0, encryptedBytes2, 0, salt.length);
                    System.arraycopy(encryptedBytes, 0, encryptedBytes2, salt.length, encryptedBytes.length);
                    if ("BASE64".equalsIgnoreCase(outputType)) {
                        return new String(Base64.getEncoder().encode(encryptedBytes2), StandardCharsets.US_ASCII);
                    } else if ("HEX".equalsIgnoreCase(outputType)) {
                        return HexUtil.encodeHexStr(encryptedBytes2);
                    } else {
                        throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.output.type.not.support", outputType));
                    }
                } else {
                    byte[] salt = saltStr.getBytes();
                    PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, keyObtentionIterations, new IvParameterSpec(iv));
                    crypto.setParams(parameterSpec);
                    if ("BASE64".equalsIgnoreCase(outputType)) {
                        return crypto.encryptBase64(source);
                    } else if ("HEX".equalsIgnoreCase(outputType)) {
                        return crypto.encryptHex(source);
                    } else {
                        throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.output.type.not.support", outputType));
                    }
                }
            } else {
                if ("BASE64".equalsIgnoreCase(outputType)) {
                    return crypto.encryptBase64(source);
                } else if ("HEX".equalsIgnoreCase(outputType)) {
                    return crypto.encryptHex(source);
                } else {
                    throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.output.type.not.support", outputType));
                }
            }
        } else {
            byte[] decode;
            if (jasypt) {
                if ("BASE64".equalsIgnoreCase(outputType)) {
                    decode = Base64.getDecoder().decode(sourceStr.getBytes(StandardCharsets.US_ASCII));
                } else if ("HEX".equalsIgnoreCase(outputType)) {
                    decode = HexUtil.decodeHex(sourceStr);
                } else {
                    throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.output.type.not.support", outputType));
                }
            } else {
                if ("BASE64".equalsIgnoreCase(outputType)) {
                    decode = Base64.getDecoder().decode(sourceStr);
                } else if ("HEX".equalsIgnoreCase(outputType)) {
                    decode = HexUtil.decodeHex(sourceStr);
                } else {
                    throw new IllegalArgumentException(MessageUtil.getMessage("convert.symmetric.output.type.not.support", outputType));
                }
            }
            if (pbe) {
                int keyObtentionIterations = 1000;
                if (iv == null) {
                    iv = new byte[0];
                }
                if (jasypt) {
                    int saltSizeBytes = 8;
                    int saltSize = Math.min(saltSizeBytes, decode.length);
                    int encMesKernelStart = Math.min(saltSizeBytes, decode.length);
                    int ivSize = saltSizeBytes < decode.length ? decode.length - saltSizeBytes : 0;
                    byte[] salt = new byte[saltSize];
                    byte[] encryptedMessageKernel = new byte[ivSize];
                    System.arraycopy(decode, 0, salt, 0, saltSize);
                    System.arraycopy(decode, encMesKernelStart, encryptedMessageKernel, 0, ivSize);
                    PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, keyObtentionIterations, new IvParameterSpec(iv));
                    crypto.setParams(parameterSpec);
                    byte[] decrypt = crypto.decrypt(encryptedMessageKernel);
                    return new String(decrypt, getCharset());
                } else {
                    byte[] salt = saltStr.getBytes();
                    PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, keyObtentionIterations, new IvParameterSpec(iv));
                    crypto.setParams(parameterSpec);
                    byte[] decrypt = crypto.decrypt(decode);
                    return new String(decrypt, getCharset());
                }
            } else {
                return new String(crypto.decrypt(decode), getCharset());
            }
        }
    }

}
