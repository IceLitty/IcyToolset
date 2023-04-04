package com.gmail.litalways.toolset.util;

import com.gmail.litalways.toolset.enums.KeyEnum;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

/**
 * 通知类
 *
 * @author IceRain
 * @since 2023/04/04
 */
public class NotificationUtil {

    private static final NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey());

    public static void info(String title, String content) {
        group.createNotification(title, content, NotificationType.INFORMATION).notify(null);
    }

    public static void info(String content) {
        group.createNotification(content, NotificationType.INFORMATION).notify(null);
    }

    public static void warning(String title, String content) {
        group.createNotification(title, content, NotificationType.WARNING).notify(null);
    }

    public static void warning(String content) {
        group.createNotification(content, NotificationType.WARNING).notify(null);
    }

    public static void error(String title, String content) {
        group.createNotification(title, content, NotificationType.ERROR).notify(null);
    }

    public static void error(String content) {
        group.createNotification(content, NotificationType.ERROR).notify(null);
    }

}
