package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.gmail.litalways.toolset.enums.KeyEnum;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author IceRain
 * @since 2022/01/26
 */
public class MainFormEncryptSymmetricFunction {

    private MainForm mainForm;

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
            String outputType = (String) this.mainForm.selectEncryptSymmetricOutputType.getModel().getSelectedItem();
            String dest = func(true, type, mode, padding, key, iv, source, outputType);
            this.mainForm.textareaEncryptSymmetricEncrypted.setText(dest);
        } catch (Exception ex) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                    .notify(null);
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
            String outputType = (String) this.mainForm.selectEncryptSymmetricOutputType.getModel().getSelectedItem();
            String dest = func(false, type, mode, padding, key, iv, source, outputType);
            this.mainForm.textareaEncryptSymmetricDecrypted.setText(dest);
        } catch (Exception ex) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                    .notify(null);
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

    private String func(boolean isEncrypt, String type, String modeStr, int paddingIndex, String keyStr, String ivStr, String sourceStr, String outputType) throws UnsupportedEncodingException {
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
            throw new IllegalArgumentException("Mode " + modeStr + " not supported.");
        }
        Padding padding;
        switch (paddingIndex) {
            case 0:
                padding = Padding.NoPadding;
                break;
            case 1:
                padding = Padding.ZeroPadding;
                break;
            case 2:
                padding = Padding.ISO10126Padding;
                break;
            case 3:
                padding = Padding.OAEPPadding;
                break;
            case 4:
                padding = Padding.PKCS1Padding;
                break;
            case 5:
                padding = Padding.PKCS5Padding;
                break;
            case 6:
                padding = Padding.SSL3Padding;
                break;
            default:
                throw new IllegalArgumentException("Padding " + this.mainForm.selectEncryptSymmetricPadding.getModel().getSelectedItem() + " not supported.");
        }
        byte[] key = keyStr.getBytes(getCharset());
        byte[] iv = null;
        if (ivStr != null && ivStr.trim().length() > 0) {
            iv = ivStr.getBytes(getCharset());
        }
        SymmetricCrypto crypto = new SymmetricCrypto(type + "/" + mode + "/" + padding, key);
        if (iv != null) {
            crypto.setIv(iv);
        }
        if (isEncrypt) {
            byte[] source = sourceStr.getBytes(getCharset());
            if ("BASE64".equalsIgnoreCase(outputType)) {
                return crypto.encryptBase64(source);
            } else if ("HEX".equalsIgnoreCase(outputType)) {
                return crypto.encryptHex(source);
            } else {
                throw new IllegalArgumentException("Output type " + outputType + " is not supported.");
            }
        } else {
            byte[] decode;
            if ("BASE64".equalsIgnoreCase(outputType)) {
                decode = Base64.getDecoder().decode(sourceStr);
            } else if ("HEX".equalsIgnoreCase(outputType)) {
                decode = HexUtil.decodeHex(sourceStr);
            } else {
                throw new IllegalArgumentException("Output type " + outputType + " is not supported.");
            }
            return new String(crypto.decrypt(decode), getCharset());
        }
    }

}
