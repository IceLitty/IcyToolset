package com.gmail.litalways.toolset.state;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 复杂配置对象
 *
 * @author IceRain
 * @since 2023/04/06
 */
@Service(Service.Level.APP)
@State(
        name = KeyConstant.CONFIG_STATE_KEY_SETTINGS,
        storages = @Storage(KeyConstant.CONFIG_STATE_FILENAME)
)
public final class MainSettingState implements PersistentStateComponent<MainSettingState> {

    public List<MainSettingsClassName> beanUtilsClassName = getDefaultBeanUtilsClassName();

    /**
     * 公开属性会保存，私有属性只会储存有getter setter的，加上此注解可以排除
     */
    @Transient
    @SuppressWarnings("unused")
    public String notSave = "defaultValue";

    /**
     * 获取实例的方法（由于该类通过application-components注入，所以需要通过对应方法获取，service中注入需要通过getService获取）
     *
     * @return 当前实例
     */
    public static MainSettingState getInstance() {
        return ApplicationManager.getApplication().getService(MainSettingState.class);
    }

    /**
     * 内部调用获取该配置实例
     *
     * @return 当前实例
     */
    @Override
    public @NotNull MainSettingState getState() {
        return this;
    }

    /**
     * 内部调用储存该配置实例
     *
     * @param mainSettingState 储存的实例
     */
    @Override
    public void loadState(@NotNull MainSettingState mainSettingState) {
        XmlSerializerUtil.copyBean(mainSettingState, this);
    }

    private static List<MainSettingsClassName> getDefaultBeanUtilsClassName() {
        List<MainSettingsClassName> list = new ArrayList<>();
        list.add(new MainSettingsClassName("", "org.springframework.beans.BeanUtils", ""));
        list.add(new MainSettingsClassName("", "org.apache.commons.beanutils.BeanUtils", ""));
        return list;
    }

}
