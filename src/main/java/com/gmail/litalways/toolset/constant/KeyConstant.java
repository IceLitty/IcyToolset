package com.gmail.litalways.toolset.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author IceRain
 * @since 2022/01/20
 */
@Getter
@AllArgsConstructor
public class KeyConstant {

    public static final String NOTIFICATION_GROUP_KEY = "com.gmail.litalways.toolset.notifications";
    // 简单配置（样例未使用）
    public static final String CONFIG_SIMPLE_TITLE = "Icy Toolset 1";
    public static final String CONFIG_SIMPLE_MAIN_KEY = "com.gmail.litalways.toolset.setting.main";
    public static final String CONFIG_SIMPLE_MAIN_LANGUAGE_KEY = "com.gmail.litalways.toolset.setting.main.language";
    // 复杂配置，没有单条配置KEY的原因是复杂配置储存了整个结构，以字段名作为KEY
    public static final String CONFIG_STATE_TITLE = "Icy Toolset";
    public static final String CONFIG_STATE_FILENAME = "IcyToolset.xml";
    public static final String CONFIG_STATE_KEY_SETTINGS = "com.gmail.litalways.toolset.settings";
    public static final String TOOL_WINDOW_STATE_FILENAME = "IcyToolset.xml";
    public static final String TOOL_WINDOW_KEY_SCRIPT = "com.gmail.litalways.toolset.script";
    // 快捷键ActionId
    public static final String ACTION_SCRIPT_EDITOR_SHORTCUT = "com.gmail.litalways.toolset.editor.script.run";

}
