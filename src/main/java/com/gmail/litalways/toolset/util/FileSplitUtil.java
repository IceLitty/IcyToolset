package com.gmail.litalways.toolset.util;

import lombok.Data;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author IceRain
 * @since 2022/11/15
 */
@Data
public class FileSplitUtil {

    boolean splitByLineBreaker = true;
    long partNo;
    long splitLength;
    File file;
    Integer readCacheSize = 1024;
    final List<Long> readBytes = new ArrayList<>();
    final Consumer<String> log;

    public FileSplitUtil(Consumer<String> log) {
        this.log = log;
    }

    public void main() {
        partNo = 1;
        String suffix = null;
        if (file.getName().lastIndexOf(".") != -1) {
            try {
                suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            } catch (Exception ignored) {}
            String suffixStr = suffix == null || suffix.trim().length() == 0 ? "" : "." + suffix;
            File outFile = new File(file.getParent(), file.getName() + "." + new BigDecimal(partNo + "").toPlainString() + suffixStr);
            readPart(file, outFile, true);
            partNo++;
            while (true) {
                outFile = new File(file.getParent(), file.getName() + "." + new BigDecimal(partNo + "").toPlainString() + suffixStr);
                if (!readPart(file, outFile, false)) {
                    break;
                }
                partNo++;
            }
        }
    }

    private boolean readPart(File file, File outFile, boolean isFirstRead) {
        long readSize = 0L;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (RandomAccessFile stream = new RandomAccessFile(file, "r")) {
            log.accept("加载文件 " + file.getPath() + (partNo == 1 ? " （首次读取）" : " （段落：" + partNo + "）"));
            if (!isFirstRead) {
                for (long l : readBytes) {
                    if (l == Integer.MAX_VALUE) {
                        stream.skipBytes(Integer.MAX_VALUE);
                        continue;
                    } else if (l < Integer.MAX_VALUE) {
                        stream.skipBytes((int) l);
                        continue;
                    }
                    double _m = l / Double.parseDouble(Integer.MAX_VALUE + "");
                    long multi = Long.max((long) _m - 1, 1);
                    long last = l - multi * Integer.MAX_VALUE;
                    for (long i = 0; i < multi; i++) {
                        stream.skipBytes(Integer.MAX_VALUE);
                    }
                    if (last > Integer.MAX_VALUE) {
                        stream.skipBytes(Integer.MAX_VALUE);
                        stream.skipBytes((int) (last - Integer.MAX_VALUE));
                    } else {
                        stream.skipBytes((int) last);
                    }
                }
            }
            boolean needSeekLineBreaker = false;
            boolean seekLineBreaker = false;
            byte[] cache = new byte[readCacheSize];
            int len;
            while ((!needSeekLineBreaker || !seekLineBreaker) && (len = stream.read(cache)) != -1) {
                if (!needSeekLineBreaker) {
                    baos.write(cache, 0, len);
                    readSize += len;
                }
                if (readSize >= splitLength) {
                    needSeekLineBreaker = true;
                }
                if (needSeekLineBreaker) {
                    if (!splitByLineBreaker) {
                        break;
                    }
                    int ch;
                    while ((ch = stream.read()) != -1) {
                        byte b = (byte) ch;
                        baos.write(b);
                        readSize++;
                        if (10 == b) { // \n
                            seekLineBreaker = true;
                            break;
                        }
                    }
                }
            }
            if (readSize == 0) {
                log.accept("段落" + partNo + "为空，文件分割结束。");
                return false;
            }
            readBytes.add(readSize);
        } catch (Exception e) {
            log.accept("读取文件发生异常：" + e.getLocalizedMessage());
            return false;
        }
        // out
        try {
            if (!outFile.exists()) {
                boolean success = outFile.createNewFile();
                if (!success) {
                    log.accept("创建文件失败：" + outFile.getPath());
                    return false;
                }
            }
        } catch (Exception e) {
            log.accept("创建文件发生异常：" + e.getLocalizedMessage());
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(outFile, false);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(baos.toByteArray());
        } catch (Exception e) {
            log.accept("写入文件发生异常：" + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

}
