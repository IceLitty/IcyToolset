package com.gmail.litalways.toolset.config;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.gui.MainSetting;
import com.gmail.litalways.toolset.state.MainSettingSimple;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 简单配置菜单
 *
 * @author IceRain
 * @since 2023/04/06
 */
public class MainSettingSimpleConfigurable implements SearchableConfigurable {

    private MainSetting mainSetting;
    private MainSettingSimple settingSimple;

    /**
     * 获取Id
     *
     * @return Id
     */
    @Override
    public @NotNull
    @NonNls String getId() {
        return KeyConstant.CONFIG_SIMPLE_MAIN_KEY;
    }

    /**
     * 获取菜单显示名称
     *
     * @return 名称
     */
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return KeyConstant.CONFIG_SIMPLE_TITLE;
    }

    /**
     * 默认选择的组件
     *
     * @return 组件
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return mainSetting.getSelectLanguage();
    }

    /**
     * 初始化组件
     *
     * @return 设置表单UI
     */
    @Override
    public @Nullable JComponent createComponent() {
        mainSetting = new MainSetting();
        settingSimple = new MainSettingSimple();
        // 初始化配置到UI
        mainSetting.getSelectLanguage().setSelectedIndex(settingSimple.getLanguage());
        return mainSetting.getPanelMain();
    }

    /**
     * 判断是否有修改，此处如果判断错误将会影响到储存
     *
     * @return 是否有修改
     */
    @Override
    public boolean isModified() {
        boolean modified = mainSetting.getSelectLanguage().getSelectedIndex() != settingSimple.getLanguage();
        modified |= !String.valueOf(mainSetting.getSelectLanguage().getSelectedIndex()).equals(String.valueOf(settingSimple.getLanguage()));
        return modified;
    }

    /**
     * 应用配置
     *
     * @throws ConfigurationException 配置异常类
     */
    @Override
    public void apply() throws ConfigurationException {
        // 储存UI配置项
        settingSimple.setLanguage(mainSetting.getSelectLanguage().getSelectedIndex());
        // 应用更改到程序
    }

    /**
     * 重置UI
     */
    @Override
    public void reset() {
        mainSetting.getSelectLanguage().setSelectedIndex(settingSimple.getLanguage());
    }

    /**
     * 删除对象
     */
    @Override
    public void disposeUIResources() {
        mainSetting = null;
        settingSimple = null;
    }

}
