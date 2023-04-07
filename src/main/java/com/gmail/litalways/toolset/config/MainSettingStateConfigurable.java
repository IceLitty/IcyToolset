package com.gmail.litalways.toolset.config;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.gui.MainSetting;
import com.gmail.litalways.toolset.state.MainSettingState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 复杂配置菜单
 *
 * @author IceRain
 * @since 2023/04/07
 */
public class MainSettingStateConfigurable implements Configurable {

    private MainSetting mainSetting;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return KeyConstant.CONFIG_STATE_TITLE;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return mainSetting.getSelectLanguage();
    }

    @Override
    public @Nullable JComponent createComponent() {
        mainSetting = new MainSetting();
        MainSettingState settingObject = MainSettingState.getInstance();
        // 初始化配置到UI
        mainSetting.getSelectLanguage().setSelectedIndex(settingObject.language);
        return mainSetting.getPanelMain();
    }

    @Override
    public boolean isModified() {
        MainSettingState settingObject = MainSettingState.getInstance();
        boolean modified = mainSetting.getSelectLanguage().getSelectedIndex() != settingObject.language;
        modified |= !String.valueOf(mainSetting.getSelectLanguage().getSelectedIndex()).equals(String.valueOf(settingObject.language));
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        MainSettingState settingObject = MainSettingState.getInstance();
        // 储存UI配置项
        settingObject.language = mainSetting.getSelectLanguage().getSelectedIndex();
        settingObject.notSave = "asd";
        // 应用更改到程序
    }

    @Override
    public void reset() {
        MainSettingState settingObject = MainSettingState.getInstance();
        mainSetting.getSelectLanguage().setSelectedIndex(settingObject.language);
    }

    @Override
    public void disposeUIResources() {
        mainSetting = null;
    }

}
