package com.gmail.litalways.toolset.util;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件浏览器工具类
 * Windows!!!
 *
 * @author IceRain
 * @since 2023/04/27
 */
public class ExplorerUtil {

    private static final String EXPLORER = "explorer";

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * 直接打开路径
     *
     * @param dirPath 路径
     * @throws IOException 异常
     */
    public static void openExplorer(String dirPath) throws IOException {
        if (dirPath == null) {
            return;
        }
        if (!isWindows()) {
            return;
        }
        Runtime.getRuntime().exec(EXPLORER + " file://" + dirPath);
    }

    /**
     * 打开文件的上级路径
     *
     * @param savedFile 文件
     * @throws IOException 异常
     */
    public static void openExplorer(File savedFile) throws IOException {
        if (savedFile == null) {
            return;
        }
        if (!isWindows()) {
            return;
        }
        Runtime.getRuntime().exec(EXPLORER + " file://" + savedFile.getParent());
    }

    /**
     * 打开VFS的上级路径
     *
     * @param savedFile 文件
     * @throws IOException 异常
     */
    public static void openExplorer(VirtualFile savedFile) throws IOException {
        if (savedFile == null) {
            return;
        }
        if (!isWindows()) {
            return;
        }
        Runtime.getRuntime().exec(EXPLORER + " " + (String.valueOf(savedFile.getParent()).startsWith("file://") ? savedFile.getParent() : "file://" + savedFile.getParent()));
    }

    /**
     * 打开文件的上级路径并选择该文件
     *
     * @param savedFile 文件
     * @throws IOException 异常
     */
    public static void openExplorerAndHighlightFile(File savedFile) throws IOException {
        if (savedFile == null) {
            return;
        }
        if (!isWindows()) {
            return;
        }
        Runtime.getRuntime().exec(EXPLORER + " /select,\"" + savedFile.getPath() + "\"");
    }

    /**
     * 打开文件的上级路径并选择该文件
     *
     * @param savedFile 文件
     * @throws IOException 异常
     */
    public static void openExplorerAndHighlightFile(VirtualFile savedFile) throws IOException {
        if (savedFile == null) {
            return;
        }
        if (!isWindows()) {
            return;
        }
        Runtime.getRuntime().exec(EXPLORER + " /select,\"" + savedFile.getPath().replace("/", "\\") + "\"");
    }

}
