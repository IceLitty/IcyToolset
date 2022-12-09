package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.enums.KeyEnum;
import com.gmail.litalways.toolset.util.FileSplitUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author IceRain
 * @since 2022/11/15
 */
public class MainFormConvertSpliterFunction {

    private final MainForm mainForm;
    private VirtualFile toSelect = null;

    public MainFormConvertSpliterFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.textConvertSpliterCacheSize.setValue(1024);
        this.mainForm.fileConvertSpliterPath.addActionListener(this::loadFile);
        this.mainForm.buttonConvertSpliterRun.addActionListener(e -> {
            Thread thread = new Thread(() -> {
                FileSplitUtil fileSplitUtil = new FileSplitUtil(new InlineLogger());
                boolean check = check(fileSplitUtil);
                if (!check) {
                    return;
                }
                try {
                    fileSplitUtil.main();
                } finally {
                    System.gc();
                }
            });
            thread.start();
        });
    }

    private void loadFile(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        this.toSelect = FileChooser.chooseFile(descriptor, null, this.toSelect);
        if (this.toSelect != null) {
            this.mainForm.fileConvertSpliterPath.setText(this.toSelect.getPath());
        }
    }

    private boolean check(FileSplitUtil fileSplitUtil) {
        String cacheSize = String.valueOf(this.mainForm.textConvertSpliterCacheSize.getValue());
        if (Pattern.compile("^\\d+$").matcher(cacheSize).matches()) {
            if (cacheSize.length() >= 19) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong cache size", null, "Cache size cannot be lower than 1 or greater than MAX_INT: " + cacheSize, NotificationType.ERROR)
                        .notify(null);
                return false;
            }
            long tmp = Long.parseLong(cacheSize);
            if (tmp < 1 || tmp > Integer.MAX_VALUE) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong cache size", null, "Cache size cannot be lower than 1 or greater than MAX_INT: " + cacheSize, NotificationType.ERROR)
                        .notify(null);
                return false;
            }
            fileSplitUtil.setReadCacheSize(Integer.parseInt(cacheSize));
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Wrong cache size", null, "Cache size must be number: " + cacheSize, NotificationType.ERROR)
                    .notify(null);
            return false;
        }
        //
        int splitCount = 1;
        String countText = this.mainForm.textConvertSpliterCount.getValue() == null ? null : String.valueOf(this.mainForm.textConvertSpliterCount.getValue());
        if (countText != null && countText.trim().length() > 0) {
            if (Pattern.compile("^\\d+$").matcher(countText).matches()) {
                splitCount = Integer.parseInt(countText);
                if (splitCount < 2) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong split count", null, "Split count must not be less than 2: " + countText, NotificationType.ERROR)
                            .notify(null);
                    return false;
                }
            } else {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong split count", null, "Split count must be number: " + countText, NotificationType.ERROR)
                        .notify(null);
                return false;
            }
        }
        //
        String splitLength = this.mainForm.textConvertSpliterSize.getValue() == null ? null : String.valueOf(this.mainForm.textConvertSpliterSize.getValue());
        if (splitLength != null && splitLength.trim().length() > 0) {
            if (Pattern.compile("^\\d+[gGmMkK]$").matcher(splitLength).matches()) {
                String number = splitLength.substring(0, splitLength.length() - 1);
                String suffix = splitLength.substring(splitLength.length() - 1);
                fileSplitUtil.setSplitLength(Long.parseLong(number));
                if (fileSplitUtil.getSplitLength() < 1) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong split length", null, "Split length must be over than 1: " + splitLength, NotificationType.ERROR)
                            .notify(null);
                    return false;
                }
                if ("g".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                                .createNotification("Wrong split length", null, "Split length must be less than " + (Long.MAX_VALUE / 1024 / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) + ": " + splitLength, NotificationType.ERROR)
                                .notify(null);
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024 * 1024 * 1024);
                } else if ("m".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                                .createNotification("Wrong split length", null, "Split length must be less than " + (Long.MAX_VALUE / 1024 / 1024 - fileSplitUtil.getReadCacheSize()) + ": " + splitLength, NotificationType.ERROR)
                                .notify(null);
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024 * 1024);
                } else if ("k".equalsIgnoreCase(suffix)) {
                    if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE / 1024 - fileSplitUtil.getReadCacheSize()) {
                        NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                                .createNotification("Wrong split length", null, "Split length must be less than " + (Long.MAX_VALUE / 1024 - fileSplitUtil.getReadCacheSize()) + ": " + splitLength, NotificationType.ERROR)
                                .notify(null);
                        return false;
                    }
                    fileSplitUtil.setSplitLength(fileSplitUtil.getSplitLength() * 1024);
                } else {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong split length", null, "Argument error: " + splitLength, NotificationType.ERROR)
                            .notify(null);
                    return false;
                }
            } else if (Pattern.compile("^\\d+$").matcher(splitLength).matches()) {
                fileSplitUtil.setSplitLength(Long.parseLong(splitLength));
                if (fileSplitUtil.getSplitLength() < 1) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong split length", null, "Split length must be over than 1: " + splitLength, NotificationType.ERROR)
                            .notify(null);
                    return false;
                } else if (fileSplitUtil.getSplitLength() >= Long.MAX_VALUE - fileSplitUtil.getReadCacheSize()) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong split length", null, "Split length must be less than " + (Long.MAX_VALUE - fileSplitUtil.getReadCacheSize()) + ": " + splitLength, NotificationType.ERROR)
                            .notify(null);
                    return false;
                }
            } else {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong split length", null, "Split length must be number: " + splitLength, NotificationType.ERROR)
                        .notify(null);
                return false;
            }
        }
        //
        if (this.toSelect == null) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Wrong input file", null, "Must select input file.", NotificationType.ERROR)
                    .notify(null);
            return false;
        } else {
            try {
                File file = new File(this.toSelect.getPath());
                if (!file.exists() || !file.isFile() || !file.canRead()) {
                    NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                            .createNotification("Wrong input file", null, "Input file is not exist, not file or not be read.", NotificationType.ERROR)
                            .notify(null);
                    return false;
                }
                fileSplitUtil.setFile(new File(this.toSelect.getPath()));
            } catch (Exception e) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong input file: " + e.getClass().getSimpleName(), null, "Init load file error: " + e.getLocalizedMessage(), NotificationType.ERROR)
                        .notify(null);
                return false;
            }
        }
        //
        if (fileSplitUtil.getSplitLength() < 1) {
            if (splitCount < 2) {
                NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                        .createNotification("Wrong split count", null, "Split count must not be less than 2: " + countText, NotificationType.ERROR)
                        .notify(null);
                return false;
            } else {
                fileSplitUtil.setSplitLength(fileSplitUtil.getFile().length() / splitCount);
            }
        }
        //
        fileSplitUtil.setSplitByLineBreaker(this.mainForm.checkConvertSpliterLineFlag.isSelected());
        return true;
    }

    static class InlineLogger implements Consumer<String> {
        @Override
        public void accept(String s) {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Progress", null, s, NotificationType.INFORMATION)
                    .notify(null);
        }
    }

}
