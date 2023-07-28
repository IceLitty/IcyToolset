package com.gmail.litalways.toolset.util;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * 通知类
 *
 * @author IceRain
 * @since 2023/04/04
 */
public class NotificationUtil {

    private static final NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup(KeyConstant.NOTIFICATION_GROUP_KEY);

    /**
     * 发送消息至指定项目
     *
     * @param title 标题
     * @param content 内容
     * @param project 项目
     */
    public static void info(String title, String content, Project project) {
        group.createNotification(title, content, NotificationType.INFORMATION).notify(project);
    }

    /**
     * 发送消息到当前项目
     *
     * @param title 标题
     * @param content 内容
     */
    public static void info(String title, String content) {
        info(title, content, null);
    }

    /**
     * 发送消息到执行项目
     *
     * @param content 内容
     * @param project 项目
     */
    public static void info(String content, Project project) {
        group.createNotification(content, NotificationType.INFORMATION).notify(project);
    }

    /**
     * 发送消息到当前项目
     *
     * @param content 内容
     */
    public static void info(String content) {
        info(content, (Project) null);
    }

    /**
     * 发送警告到执行项目
     *
     * @param title 标题
     * @param content 内容
     * @param project 项目
     */
    public static void warning(String title, String content, Project project) {
        group.createNotification(title, content, NotificationType.WARNING).notify(project);
    }

    /**
     * 发送警告到当前项目
     *
     * @param title 标题
     * @param content 内容
     */
    public static void warning(String title, String content) {
        warning(title, content, null);
    }

    /**
     * 发送警告到执行项目
     *
     * @param content 内容
     * @param project 项目
     */
    public static void warning(String content, Project project) {
        group.createNotification(content, NotificationType.WARNING).notify(project);
    }

    /**
     * 发送警告到当前项目
     *
     * @param content 内容
     */
    public static void warning(String content) {
        warning(content, (Project) null);
    }

    /**
     * 发送错误到指定项目
     *
     * @param title 标题
     * @param content 内容
     * @param project 项目
     */
    public static void error(String title, String content, Project project) {
        group.createNotification(title, content == null ? "Null pointer exception" : content, NotificationType.ERROR).notify(project);
    }

    /**
     * 发送错误到当前项目
     *
     * @param title 标题
     * @param content 内容
     */
    public static void error(String title, String content) {
        error(title, content, null);
    }

    /**
     * 发送错误到指定项目
     *
     * @param content 内容
     * @param project 项目
     */
    public static void error(String content, Project project) {
        group.createNotification(content, NotificationType.ERROR).notify(project);
    }

    /**
     * 发送错误到当前项目
     *
     * @param content 内容
     */
    public static void error(String content) {
        error(content, (Project) null);
    }

}
