package com.gmail.litalways.toolset.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jdesktop.swingx.JXImageView;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件剪切板
 * {@link JXImageView.ImageTransferable}
 *
 * @author IceRain
 * @since 2024/8/5
 */
@SuppressWarnings("JavadocReference")
public class FileTransferable implements Transferable {

    @Data
    @AllArgsConstructor
    public static class File {
        private String fileName;
        private String fileExtension;
        private byte[] fileData;
    }

    private final File[] data;

    public FileTransferable(File... data) {
        this.data = data;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return dataFlavor == DataFlavor.javaFileListFlavor;
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
        if (dataFlavor == DataFlavor.javaFileListFlavor) {
            if (this.data == null) {
                return new ArrayList<>();
            } else {
                List<java.io.File> files = new ArrayList<>();
                for (File datum : this.data) {
                    java.io.File file = java.io.File.createTempFile(datum.getFileName(), "." + datum.getFileExtension());
                    Files.write(file.toPath(), datum.getFileData(), StandardOpenOption.WRITE);
                    files.add(file);
                }
                return files;
            }
        } else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }

}
