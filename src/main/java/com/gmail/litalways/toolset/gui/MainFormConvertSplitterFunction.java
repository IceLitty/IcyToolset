package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.util.ExplorerUtil;
import com.gmail.litalways.toolset.util.FileSplitUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author IceRain
 * @since 2022/11/15
 */
public class MainFormConvertSplitterFunction {

    private final ToolWindowConvert component;
    private VirtualFile toSelect = null;

    public MainFormConvertSplitterFunction(ToolWindowConvert component) {
        this.component = component;
        this.component.textConvertSplitterCacheSize.setValue(1024);
        this.component.fileConvertSplitterPath.addActionListener(this::loadFile);
        this.component.buttonConvertSplitterClear.addActionListener(e -> {
            this.component.textConvertSplitterCacheSize.setValue(1024);
            this.toSelect = null;
            this.component.fileConvertSplitterPath.setText(null);
            this.component.textConvertSplitterCount.setValue(null);
            this.component.textConvertSplitterSize.setValue(null);
        });
        this.component.buttonConvertSplitterRun.addActionListener(e -> {
            Thread thread = new Thread(() -> {
                FileSplitUtil fileSplitUtil = new FileSplitUtil(new InlineLogger());
                boolean check = check(fileSplitUtil);
                if (!check) {
                    return;
                }
                try {
                    fileSplitUtil.runProgress();
                } finally {
                    System.gc();
                    if (this.component.checkConvertSplitterOpenDirectory.isSelected()) {
                        try {
                            ExplorerUtil.openExplorer(this.toSelect);
                        } catch (IOException ex) {
                            NotificationUtil.error(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
                        }
                    }
                }
            });
            thread.start();
        });
    }

    private void loadFile(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
        if (this.toSelect != null) {
            this.component.fileConvertSplitterPath.setText(this.toSelect.getPath());
        }
    }

    private boolean check(FileSplitUtil fileSplitUtil) {
        String cacheSize = String.valueOf(this.component.textConvertSplitterCacheSize.getValue());
        if (Pattern.compile("^\\d+$").matcher(cacheSize).matches()) {
            try {
                long tmp = Long.parseLong(cacheSize);
                if (tmp < 1 || tmp > Integer.MAX_VALUE) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.cache.size", cacheSize));
                    return false;
                }
            } catch (NumberFormatException e) {
                NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.cache.size", cacheSize));
                return false;
            }
            fileSplitUtil.setReadCacheSize(Integer.parseInt(cacheSize));
        } else {
            NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.cache.size", cacheSize));
            return false;
        }
        //
        int splitCount = 1;
        String countText = this.component.textConvertSplitterCount.getValue() == null ? null : String.valueOf(this.component.textConvertSplitterCount.getValue());
        if (countText != null && !countText.trim().isEmpty()) {
            if (Pattern.compile("^\\d+$").matcher(countText).matches()) {
                splitCount = Integer.parseInt(countText);
                if (splitCount < 2) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.count", countText));
                    return false;
                }
            } else {
                NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.count", countText));
                return false;
            }
        }
        //
        String splitLength = this.component.textConvertSplitterSize.getValue() == null ? null : String.valueOf(this.component.textConvertSplitterSize.getValue());
        if (splitLength != null && !splitLength.trim().isEmpty()) {
            if (Pattern.compile("^\\d+[gGmMkK]$").matcher(splitLength).matches()) {
                String number = splitLength.substring(0, splitLength.length() - 1);
                String suffix = splitLength.substring(splitLength.length() - 1);
                fileSplitUtil.setSplitLength(Long.parseLong(number));
                if (fileSplitUtil.getSplitLength() < 1) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.over.than", splitLength));
                    return false;
                }
                if ("g".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.less.than", Long.MAX_VALUE / 1024 / 1024 / 1024 - fileSplitUtil.getReadCacheSize(), splitLength));
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024 * 1024 * 1024);
                } else if ("m".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.less.than", Long.MAX_VALUE / 1024 / 1024 - fileSplitUtil.getReadCacheSize(), splitLength));
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024 * 1024);
                } else if ("k".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.less.than", Long.MAX_VALUE / 1024 - fileSplitUtil.getReadCacheSize(), splitLength));
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024);
                } else {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length", splitLength));
                    return false;
                }
            } else if (Pattern.compile("^\\d+$").matcher(splitLength).matches()) {
                fileSplitUtil.setSplitLength(Long.parseLong(splitLength));
                if (fileSplitUtil.getSplitLength() < 1) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.over.than", splitLength));
                    return false;
                } else if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE - fileSplitUtil.getReadCacheSize()) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length.less.than", Long.MAX_VALUE - fileSplitUtil.getReadCacheSize(), splitLength));
                    return false;
                }
            } else {
                NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.length", splitLength));
                return false;
            }
        }
        //
        if (this.toSelect == null) {
            NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.no.input.file"));
            return false;
        } else {
            try {
                File file = new File(this.toSelect.getPath());
                if (!file.exists() || !file.isFile() || !file.canRead()) {
                    NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.input.file.not.exist"));
                    return false;
                }
                fileSplitUtil.setFile(new File(this.toSelect.getPath()));
            } catch (Exception e) {
                NotificationUtil.error(e.getClass().getSimpleName(), MessageUtil.getMessage("convert.splitter.tip.input.file.load.error", e.getLocalizedMessage()));
                return false;
            }
        }
        //
        if (fileSplitUtil.getSplitLength() < 1) {
            if (splitCount < 2) {
                NotificationUtil.error(MessageUtil.getMessage("convert.splitter.tip.wrong.split.count", countText));
                return false;
            } else {
                fileSplitUtil.setSplitLength(fileSplitUtil.getFile().length() / splitCount);
            }
        }
        //
        fileSplitUtil.setSplitByLineBreaker(this.component.checkConvertSplitterLineFlag.isSelected());
        return true;
    }

    static class InlineLogger implements Consumer<String> {
        @Override
        public void accept(String s) {
            NotificationUtil.info(MessageUtil.getMessage("convert.splitter.tip.progress.title"), s);
        }
    }

}
