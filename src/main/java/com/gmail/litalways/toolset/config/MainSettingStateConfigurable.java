package com.gmail.litalways.toolset.config;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.gui.MainSetting;
import com.gmail.litalways.toolset.state.MainSettingState;
import com.gmail.litalways.toolset.state.MainSettingsClassName;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 复杂配置菜单
 *
 * @author IceRain
 * @since 2023/04/07
 */
public class MainSettingStateConfigurable implements Configurable {

    private MainSetting mainSetting;
    private List<MainSettingsClassName> lastSaved;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return KeyConstant.CONFIG_STATE_TITLE;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return mainSetting.getBeanUtilsClassTable();
    }

    @Override
    public @Nullable JComponent createComponent() {
        MainSettingState settingObject = MainSettingState.getInstance();
        mainSetting = new MainSetting(settingObject);
        lastSaved = new ArrayList<>(settingObject.beanUtilsClassName);
        // 初始化配置到UI
        mainSetting.loadTableData(lastSaved);
        return mainSetting.getPanelMain();
    }

    @Override
    @SuppressWarnings("CommentedOutCode")
    public boolean isModified() {
        boolean modified = false;
        List<MainSettingsClassName> newTableDataValue = mainSetting.getTableDataValue();
        if (newTableDataValue.size() != lastSaved.size()) {
            modified = true;
        } else {
            for (MainSettingsClassName saved : lastSaved) {
                boolean foundSame = false;
                for (MainSettingsClassName ui : newTableDataValue) {
                    if (saved.equals(ui)) {
                        foundSame = true;
                        break;
                    }
                }
                if (!foundSame) {
                    modified = true;
                    break;
                }
            }
        }
//        boolean modified = mainSetting.getBeanUtilsClassTable().getSelectedIndex() != settingObject.language;
//        modified |= !String.valueOf(mainSetting.getBeanUtilsClassTable().getSelectedIndex()).equals(String.valueOf(settingObject.language));
        return modified;
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public void apply() throws ConfigurationException {
        MainSettingState settingObject = MainSettingState.getInstance();
        // 储存UI配置项
        synchronized (MainSettingState.class) {
            settingObject.beanUtilsClassName.clear();
            settingObject.beanUtilsClassName.addAll(mainSetting.getTableDataValue());
            lastSaved.clear();
            lastSaved.addAll(settingObject.beanUtilsClassName);
        }
        // 应用更改到程序
    }

    @Override
    public void reset() {
        MainSettingState settingObject = MainSettingState.getInstance();
        mainSetting.loadTableData(settingObject.beanUtilsClassName);
    }

    @Override
    public void disposeUIResources() {
        mainSetting = null;
    }

}
