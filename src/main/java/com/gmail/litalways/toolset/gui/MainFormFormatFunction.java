package com.gmail.litalways.toolset.gui;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gmail.litalways.toolset.enums.KeyEnum;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import org.w3c.dom.Document;

import java.awt.event.ActionEvent;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormFormatFunction {

    private final MainForm mainForm;

    public MainFormFormatFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.buttonFormat.addActionListener(this::format);
    }

    private void format(ActionEvent e) {
        String text = this.mainForm.textareaFormat.getText();
        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
        if (tmp.length() < 1) {
            return;
        }
        if ("{".equals(tmp.substring(0, 1))) {
            JSONObject jsonObject = JSONUtil.parseObj(text);
            text = jsonObject.toJSONString(4);
            this.mainForm.textareaFormat.setText(text);
        } else if ("[".equals(tmp.substring(0, 1))) {
            JSONArray jsonArray = JSONUtil.parseArray(text);
            text = jsonArray.toJSONString(4);
            this.mainForm.textareaFormat.setText(text);
        } else if ("<".equals(tmp.substring(0, 1))) {
            Document document = XmlUtil.parseXml(text);
            text = XmlUtil.toStr(document, true);
            this.mainForm.textareaFormat.setText(text);
        } else {
            NotificationGroupManager.getInstance().getNotificationGroup(KeyEnum.NOTIFICATION_GROUP_KEY.getKey())
                    .createNotification("Only support JSON and XML now!", null, null, NotificationType.ERROR)
                    .notify(null);
        }
    }

}
