package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.filter.PngFileFilter;
import com.gmail.litalways.toolset.util.ExplorerUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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
public class MainFormQRFunction {

    private final ToolWindowQRCode component;
    private VirtualFile toSelect = null;
    private String lastSaveImgPath = null;

    public MainFormQRFunction(ToolWindowQRCode component) {
        this.component = component;
        this.component.radioZxingQr.setSelected(true);
        this.component.textZxingWidth.setValue(200);
        this.component.textZxingHeight.setValue(200);
        this.component.radioZxingQr.addActionListener(e -> {
            this.component.textZxingWidth.setValue(200);
            this.component.textZxingHeight.setValue(200);
        });
        this.component.radioZxingBar.addActionListener(e -> {
            this.component.textZxingWidth.setValue(500);
            this.component.textZxingHeight.setValue(200);
        });
        this.component.radioZxingMatrix.addActionListener(e -> {
            this.component.textZxingWidth.setValue(200);
            this.component.textZxingHeight.setValue(200);
        });
        this.component.buttonZxingToFile.addActionListener(e -> {
            encode();
        });
        this.component.fileZxingFromFile.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false);
            this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
            if (this.toSelect != null) {
                this.component.fileZxingFromFile.setText(this.toSelect.getPresentableUrl());
                decode();
                if (this.lastSaveImgPath == null) {
                    this.lastSaveImgPath = this.toSelect.getPath();
                }
            }
        });
    }

    private String getCharset() {
        int encodingModelIndex = this.component.selectZxingEncoding.getSelectedIndex();
        Object selectedObjects = this.component.selectZxingEncoding.getModel().getSelectedItem();
        String charset;
        if (encodingModelIndex == 0) {
            charset = System.getProperty("file.encoding");
        } else {
            charset = (String) selectedObjects;
        }
        return charset;
    }

    private void encode() {
        String content = this.component.textareaZxingText.getText();
        BarcodeFormat barcodeFormat;
        if (this.component.radioZxingQr.isSelected()) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        } else if (this.component.radioZxingBar.isSelected()) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (this.component.radioZxingMatrix.isSelected()) {
            barcodeFormat = BarcodeFormat.DATA_MATRIX;
        } else {
            NotificationUtil.error(MessageUtil.getMessage("qr.tip.type.not.support"));
            return;
        }
        Integer width = this.component.textZxingWidth.getValue() == null ? null : Integer.parseInt(String.valueOf(this.component.textZxingWidth.getValue()));
        Integer height = this.component.textZxingHeight.getValue() == null ? null : Integer.parseInt(String.valueOf(this.component.textZxingHeight.getValue()));
        if (width == null || height == null) {
            NotificationUtil.error(MessageUtil.getMessage("qr.tip.width.height.not.specified"));
            return;
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, getCharset());
        int errorCorrectionIndex = this.component.selectZxingErrorCorrection.getSelectedIndex();
        switch (errorCorrectionIndex) {
            case 0 -> hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            case 1 -> hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            case 2 -> hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            case 3 -> hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            default -> {
            }
        }
        hints.put(EncodeHintType.MARGIN, "1");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, barcodeFormat, width, height, hints);
        } catch (WriterException ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            return;
        }
        JFileChooser fileChooser = new JFileChooser(this.lastSaveImgPath);
        fileChooser.setFileFilter(new PngFileFilter());
        int status = fileChooser.showSaveDialog(this.component.panelMain);
        if (status == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".png")) {
                    file = new File(file.getCanonicalPath() + ".png");
                }
                MatrixToImageWriter.writeToPath(bitMatrix, "png", file.toPath());
                this.lastSaveImgPath = file.getPath();
                if (this.component.checkZxingOpenDirectory.isSelected()) {
                    ExplorerUtil.openExplorerAndHighlightFile(file);
                }
            } catch (Exception ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            }
        }
    }

    private void decode() {
        BarcodeFormat barcodeFormat = null;
        if (this.component.radioZxingQr.isSelected()) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        } else if (this.component.radioZxingBar.isSelected()) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (this.component.radioZxingMatrix.isSelected()) {
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
            this.component.textareaZxingText.setText(result.getText());
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

}
