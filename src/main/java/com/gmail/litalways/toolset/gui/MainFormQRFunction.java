package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.filter.PngFileFilter;
import com.gmail.litalways.toolset.util.ExplorerUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private VirtualFile toSelectMaskOn = null;
    private VirtualFile toSelectMaskOff = null;
    private VirtualFile toSelectLogo = null;
    private String lastSaveImgPath = null;

    public MainFormQRFunction(ToolWindowQRCode component) {
        this.component = component;
        this.component.radioZxingQr.setSelected(true);
        this.component.textZxingWidth.setValue(200);
        this.component.textZxingHeight.setValue(200);
        this.component.radioZxingQr.addActionListener(e -> {
            this.component.textZxingWidth.setValue(200);
            this.component.textZxingHeight.setValue(200);
            this.component.checkMaskOnUseFile.setEnabled(true);
            this.component.checkMaskOnUseColor.setEnabled(true);
            this.component.checkMaskOffUseFile.setEnabled(true);
            this.component.checkMaskOffUseColor.setEnabled(true);
            this.component.checkLogo.setEnabled(true);
            this.component.checkMaskOnUseFile.setSelected(false);
            this.component.checkMaskOnUseColor.setSelected(false);
            this.component.checkMaskOffUseFile.setSelected(false);
            this.component.checkMaskOffUseColor.setSelected(false);
            this.component.checkLogo.setSelected(false);
        });
        this.component.radioZxingBar.addActionListener(e -> {
            this.component.textZxingWidth.setValue(500);
            this.component.textZxingHeight.setValue(200);
            for (Component c : this.component.panelQrOptions.getComponents()) {
                c.setEnabled(false);
            }
            this.component.checkMaskOnUseFile.setSelected(false);
            this.component.checkMaskOnUseColor.setSelected(false);
            this.component.checkMaskOffUseFile.setSelected(false);
            this.component.checkMaskOffUseColor.setSelected(false);
            this.component.checkLogo.setSelected(false);
        });
        this.component.radioZxingMatrix.addActionListener(e -> {
            this.component.textZxingWidth.setValue(200);
            this.component.textZxingHeight.setValue(200);
            for (Component c : this.component.panelQrOptions.getComponents()) {
                c.setEnabled(false);
            }
            this.component.checkMaskOnUseFile.setSelected(false);
            this.component.checkMaskOnUseColor.setSelected(false);
            this.component.checkMaskOffUseFile.setSelected(false);
            this.component.checkMaskOffUseColor.setSelected(false);
            this.component.checkLogo.setSelected(false);
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
        this.component.buttonZxingClean.addActionListener(e -> this.clean());
        this.component.colorMaskOn.setSupportTransparency(true);
        this.component.colorMaskOff.setSupportTransparency(true);
        this.component.fileMaskOn.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            this.toSelectMaskOn = FileChooser.chooseFile(descriptor, null, this.toSelectMaskOn);
            if (this.toSelectMaskOn != null) {
                this.component.fileMaskOn.setText(this.toSelectMaskOn.getPresentableUrl());
            }
        });
        this.component.fileMaskOff.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            this.toSelectMaskOff = FileChooser.chooseFile(descriptor, null, this.toSelectMaskOff);
            if (this.toSelectMaskOff != null) {
                this.component.fileMaskOff.setText(this.toSelectMaskOff.getPresentableUrl());
            }
        });
        this.component.fileLogo.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            this.toSelectLogo = FileChooser.chooseFile(descriptor, null, this.toSelectLogo);
            if (this.toSelectLogo != null) {
                this.component.fileLogo.setText(this.toSelectLogo.getPresentableUrl());
            }
        });
        this.component.checkMaskOnUseFile.addActionListener(e -> {
            if (this.component.checkMaskOnUseFile.isSelected()) {
                this.component.checkMaskOnUseColor.setSelected(false);
                this.component.colorMaskOn.setEnabled(false);
                this.component.fileMaskOn.setEnabled(true);
            } else {
                this.component.fileMaskOn.setEnabled(false);
            }
        });
        this.component.checkMaskOnUseColor.addActionListener(e -> {
            if (this.component.checkMaskOnUseColor.isSelected()) {
                this.component.checkMaskOnUseFile.setSelected(false);
                this.component.fileMaskOn.setEnabled(false);
                this.component.colorMaskOn.setEnabled(true);
            } else {
                this.component.colorMaskOn.setEnabled(false);
            }
        });
        this.component.checkMaskOffUseFile.addActionListener(e -> {
            if (this.component.checkMaskOffUseFile.isSelected()) {
                this.component.checkMaskOffUseColor.setSelected(false);
                this.component.colorMaskOff.setEnabled(false);
                this.component.fileMaskOff.setEnabled(true);
            } else {
                this.component.fileMaskOff.setEnabled(false);
            }
        });
        this.component.checkMaskOffUseColor.addActionListener(e -> {
            if (this.component.checkMaskOffUseColor.isSelected()) {
                this.component.checkMaskOffUseFile.setSelected(false);
                this.component.fileMaskOff.setEnabled(false);
                this.component.colorMaskOff.setEnabled(true);
            } else {
                this.component.colorMaskOff.setEnabled(false);
            }
        });
        this.component.checkLogo.addActionListener(e -> {
            this.component.fileLogo.setEnabled(this.component.checkLogo.isSelected());
            this.component.textLogoSizeFactor.setEnabled(this.component.checkLogo.isSelected());
        });
        this.component.textLogoSizeFactor.setValue("5.5");
    }

    private void clean() {
        this.toSelect = null;
        this.component.fileZxingFromFile.setText("");
        this.component.textareaZxingText.setText("");
        this.toSelectMaskOn = null;
        this.toSelectMaskOff = null;
        this.toSelectLogo = null;
        this.component.fileMaskOn.setText("");
        this.component.fileMaskOff.setText("");
        this.component.fileLogo.setText("");
        this.component.textLogoSizeFactor.setValue("5.5");
        for (Component c : this.component.panelQrOptions.getComponents()) {
            c.setEnabled(false);
        }
        this.component.checkMaskOnUseFile.setEnabled(true);
        this.component.checkMaskOnUseColor.setEnabled(true);
        this.component.checkMaskOffUseFile.setEnabled(true);
        this.component.checkMaskOffUseColor.setEnabled(true);
        this.component.checkLogo.setEnabled(true);
        this.component.checkMaskOnUseFile.setSelected(false);
        this.component.checkMaskOnUseColor.setSelected(false);
        this.component.checkMaskOffUseFile.setSelected(false);
        this.component.checkMaskOffUseColor.setSelected(false);
        this.component.checkLogo.setSelected(false);
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
        if (barcodeFormat == BarcodeFormat.DATA_MATRIX) {
            hints.put(EncodeHintType.DATA_MATRIX_COMPACT, true);
        }
        BitMatrix bitMatrix;
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
                if (this.component.radioZxingQr.isSelected() && (
                        this.component.checkMaskOnUseFile.isSelected() ||
                        this.component.checkMaskOnUseColor.isSelected() ||
                        this.component.checkMaskOffUseFile.isSelected() ||
                        this.component.checkMaskOffUseColor.isSelected() ||
                        this.component.checkLogo.isSelected()
                        )) {
                    writeMatrixWithQrCustom(bitMatrix, file);
                } else {
                    MatrixToImageWriter.writeToPath(bitMatrix, "png", file.toPath());
                }
                this.lastSaveImgPath = file.getPath();
                if (this.component.checkZxingOpenDirectory.isSelected()) {
                    ExplorerUtil.openExplorerAndHighlightFile(file);
                }
            } catch (Exception ex) {
                NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
            }
        }
    }

    private void writeMatrixWithQrCustom(BitMatrix bitMatrix, File dest) throws IOException {
        BufferedImage maskOnUsedBufferedImage = null;
        BufferedImage maskOffUsedBufferedImage = null;
        int onColor = 0xFF000000; // default qr on color is black
        int offColor = 0xFFFFFFFF; // default qr off color is white
        if (this.component.checkMaskOnUseFile.isSelected()) {
            try (InputStream fileIs = this.toSelectMaskOn.getInputStream()) {
                BufferedImage maskBufferedImage = ImageIO.read(fileIs);
                // scale mask img
                double maskWidthScaleFactor = new BigDecimal(String.valueOf(bitMatrix.getWidth())).divide(new BigDecimal(String.valueOf(maskBufferedImage.getWidth())), 2, RoundingMode.UP).doubleValue();
                double maskHeightScaleFactor = new BigDecimal(String.valueOf(bitMatrix.getHeight())).divide(new BigDecimal(String.valueOf(maskBufferedImage.getHeight())), 2, RoundingMode.UP).doubleValue();
                AffineTransform maskAt = new AffineTransform();
                maskAt.setToScale(maskWidthScaleFactor, maskHeightScaleFactor);
                AffineTransformOp maskAtOp = new AffineTransformOp(maskAt, AffineTransformOp.TYPE_BILINEAR);
                maskOnUsedBufferedImage = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
                maskOnUsedBufferedImage = maskAtOp.filter(maskBufferedImage, maskOnUsedBufferedImage);
            } catch (Exception ex) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.mask.on.file"), ex.getClass().getName() + ex.getLocalizedMessage());
                return;
            }
        } else if (this.component.checkMaskOnUseColor.isSelected()) {
            if (this.component.colorMaskOn.getSelectedColor() == null) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.color.on.empty"));
                return;
            }
            onColor = this.component.colorMaskOn.getSelectedColor().getRGB();
        }
        if (this.component.checkMaskOffUseFile.isSelected()) {
            try (InputStream fileIs = this.toSelectMaskOff.getInputStream()) {
                BufferedImage maskBufferedImage = ImageIO.read(fileIs);
                // scale mask img
                double maskWidthScaleFactor = new BigDecimal(String.valueOf(bitMatrix.getWidth())).divide(new BigDecimal(String.valueOf(maskBufferedImage.getWidth())), 2, RoundingMode.UP).doubleValue();
                double maskHeightScaleFactor = new BigDecimal(String.valueOf(bitMatrix.getHeight())).divide(new BigDecimal(String.valueOf(maskBufferedImage.getHeight())), 2, RoundingMode.UP).doubleValue();
                AffineTransform maskAt = new AffineTransform();
                maskAt.setToScale(maskWidthScaleFactor, maskHeightScaleFactor);
                AffineTransformOp maskAtOp = new AffineTransformOp(maskAt, AffineTransformOp.TYPE_BILINEAR);
                maskOffUsedBufferedImage = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
                maskOffUsedBufferedImage = maskAtOp.filter(maskBufferedImage, maskOffUsedBufferedImage);
            } catch (Exception ex) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.mask.off.file"), ex.getClass().getName() + ex.getLocalizedMessage());
                return;
            }
        } else if (this.component.checkMaskOffUseColor.isSelected()) {
            if (this.component.colorMaskOff.getSelectedColor() == null) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.color.off.empty"));
                return;
            }
            offColor = this.component.colorMaskOff.getSelectedColor().getRGB();
        }
        // apply mask
        BufferedImage bitBufferedImage = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] rowPixels = new int[bitBufferedImage.getWidth()];
        BitArray row = null;
        for (int y = 0; y < bitBufferedImage.getHeight(); y++) {
            row = bitMatrix.getRow(y, row);
            for (int x = 0; x < bitBufferedImage.getWidth(); x++) {
                if (row.get(x)) {
                    // on bit
                    rowPixels[x] = maskOnUsedBufferedImage == null ? onColor : maskOnUsedBufferedImage.getRGB(x, y);
                } else {
                    // off bit
                    rowPixels[x] = maskOffUsedBufferedImage == null ? offColor : maskOffUsedBufferedImage.getRGB(x, y);
                }
            }
            bitBufferedImage.setRGB(0, y, bitBufferedImage.getWidth(), 1, rowPixels, 0, bitBufferedImage.getWidth());
        }
        if (this.component.checkLogo.isSelected()) {
            Double logoSize = this.component.textLogoSizeFactor.getValue() == null ? null : Double.parseDouble(String.valueOf(this.component.textLogoSizeFactor.getValue()));
            if (logoSize == null) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.logo.size.empty"));
                return;
            }
            try (InputStream fileIs = this.toSelectLogo.getInputStream()) {
                BufferedImage logoBufferedImage = ImageIO.read(fileIs);
                // scale logo img
                int logoWidth = new BigDecimal(String.valueOf(bitBufferedImage.getWidth())).divide(new BigDecimal(logoSize), 2, RoundingMode.UP).intValue();
                int logoHeight = new BigDecimal(String.valueOf(bitBufferedImage.getHeight())).divide(new BigDecimal(logoSize), 2, RoundingMode.UP).intValue();
                double logoWidthScaleFactor = new BigDecimal(String.valueOf(logoWidth)).divide(new BigDecimal(String.valueOf(logoBufferedImage.getWidth())), 2, RoundingMode.UP).doubleValue();
                double logoHeightScaleFactor = new BigDecimal(String.valueOf(logoHeight)).divide(new BigDecimal(String.valueOf(logoBufferedImage.getHeight())), 2, RoundingMode.UP).doubleValue();
                AffineTransform logoAt = new AffineTransform();
                logoAt.setToScale(logoWidthScaleFactor, logoHeightScaleFactor);
                AffineTransformOp logoAtOp = new AffineTransformOp(logoAt, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage logoUsedBufferedImage = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
                logoUsedBufferedImage = logoAtOp.filter(logoBufferedImage, logoUsedBufferedImage);
                // apply logo
                int logoPosX = (bitBufferedImage.getWidth() - logoUsedBufferedImage.getWidth()) / 2;
                int logoPosY = (bitBufferedImage.getHeight() - logoUsedBufferedImage.getHeight()) / 2;
                Graphics2D bitBufferedImageGraphics = bitBufferedImage.createGraphics();
                bitBufferedImageGraphics.drawImage(logoUsedBufferedImage, logoPosX, logoPosY, logoUsedBufferedImage.getWidth(), logoUsedBufferedImage.getHeight(), null);
                bitBufferedImageGraphics.dispose();
            } catch (Exception ex) {
                NotificationUtil.error(MessageUtil.getMessage("qr.tip.qr.error.logo.file"), ex.getClass().getName() + ex.getLocalizedMessage());
                return;
            }
        }
        // save
        ImageIO.write(bitBufferedImage, "png", dest);
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
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, getCharset());
            hints.put(DecodeHintType.TRY_HARDER, true);
            hints.put(DecodeHintType.ALSO_INVERTED, true);
            if (barcodeFormat != null) {
                hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(barcodeFormat));
            }
            BufferedImage bufferedImage = ImageIO.read(fileIs);
            LuminanceSource luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            Binarizer binarizer = new HybridBinarizer(luminanceSource);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Result result;
            try {
                result = new MultiFormatReader().decode(binaryBitmap, hints);
            } catch (NotFoundException e) {
                binarizer = new HybridBinarizer(luminanceSource.invert());
                binaryBitmap = new BinaryBitmap(binarizer);
                result = new MultiFormatReader().decode(binaryBitmap, hints);
            }
            this.component.textareaZxingText.setText(result.getText());
        } catch (Exception ex) {
            NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

}
