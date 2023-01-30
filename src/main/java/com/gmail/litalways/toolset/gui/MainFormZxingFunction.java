package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.enums.KeyEnum;
import com.gmail.litalways.toolset.filter.PngFileFilter;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IceRain
 * @since 2022/01/28
 */
public class MainFormZxingFunction {

    private final MainForm mainForm;
    private VirtualFile toSelect = null;
    private String lastSaveImgPath = null;

    public MainFormZxingFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.radioZxingQr.setSelected(true);
        this.mainForm.textZxingWidth.setValue(200);
        this.mainForm.textZxingHeight.setValue(200);
        this.mainForm.radioZxingQr.addActionListener(e -> {
            this.mainForm.textZxingWidth.setValue(200);
            this.mainForm.textZxingHeight.setValue(200);
        });
        this.mainForm.radioZxingBar.addActionListener(e -> {
            this.mainForm.textZxingWidth.setValue(500);
            this.mainForm.textZxingHeight.setValue(200);
        });
        this.mainForm.radioZxingMatrix.addActionListener(e -> {
            this.mainForm.textZxingWidth.setValue(200);
            this.mainForm.textZxingHeight.setValue(200);
        });
        this.mainForm.buttonZxingToFile.addActionListener(e -> {
            encode();
        });
        this.mainForm.fileZxingFromFile.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
            this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
            if (this.toSelect != null) {
                this.mainForm.fileZxingFromFile.setText(this.toSelect.getPresentableUrl());
                decode();
                if (this.lastSaveImgPath == null) {
                    this.lastSaveImgPath = this.toSelect.getPath();
                }
            }
        });
    }

    private String getCharset() {
        int encodingModelIndex = this.mainForm.selectZxingEncoding.getSelectedIndex();
        Object selectedObjects = this.mainForm.selectZxingEncoding.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private void encode() {
        String content = this.mainForm.textareaZxingText.getText();
        BarcodeFormat barcodeFormat;
        if (this.mainForm.radioZxingQr.isSelected()) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        } else if (this.mainForm.radioZxingBar.isSelected()) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (this.mainForm.radioZxingMatrix.isSelected()) {
            barcodeFormat = BarcodeFormat.DATA_MATRIX;
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Bar code type not supported!", null, null, NotificationType.ERROR)
                    .notify(null);
            return;
        }
        Integer width = this.mainForm.textZxingWidth.getValue() == null ? null : Integer.parseInt(String.valueOf(this.mainForm.textZxingWidth.getValue()));
        Integer height = this.mainForm.textZxingHeight.getValue() == null ? null : Integer.parseInt(String.valueOf(this.mainForm.textZxingHeight.getValue()));
        if (width == null || height == null) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Image width or height can not be null!", null, null, NotificationType.ERROR)
                    .notify(null);
            return;
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, getCharset());
        int errorCorrectionIndex = this.mainForm.selectZxingErrorCorrection.getSelectedIndex();
        switch (errorCorrectionIndex) {
            case 0:
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                break;
            case 1:
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
                break;
            case 2:
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                break;
            case 3:
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                break;
            default:
                break;
        }
        hints.put(EncodeHintType.MARGIN, "1");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, barcodeFormat, width, height, hints);
        } catch (WriterException ex) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                    .notify(null);
            return;
        }
        JFileChooser fileChooser = new JFileChooser(this.lastSaveImgPath);
        fileChooser.setFileFilter(new PngFileFilter());
        int status = fileChooser.showSaveDialog(this.mainForm.panelMain);
        if (status == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".png")) {
                    file = new File(file.getCanonicalPath() + ".png");
                }
                MatrixToImageWriter.writeToPath(bitMatrix, "png", file.toPath());
                this.lastSaveImgPath = file.getPath();
            } catch (Exception ex) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
            }
        }
    }

    private void decode() {
        BarcodeFormat barcodeFormat = null;
        if (this.mainForm.radioZxingQr.isSelected()) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        } else if (this.mainForm.radioZxingBar.isSelected()) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (this.mainForm.radioZxingMatrix.isSelected()) {
            barcodeFormat = BarcodeFormat.DATA_MATRIX;
        }
        try (InputStream fileIs = this.toSelect.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(fileIs);
            LuminanceSource luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            Binarizer binarizer = new HybridBinarizer(luminanceSource);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, getCharset());
            if (barcodeFormat != null) {
                hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(barcodeFormat));
            }
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            this.mainForm.textareaZxingText.setText(result.getText());
        } catch (Exception ex) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification(ex.getClass().getName(), null, ex.getLocalizedMessage(), NotificationType.ERROR)
                    .notify(null);
        }
    }

}
