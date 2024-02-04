package com.gmail.litalways.toolset.state;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具窗口脚本功能储存类
 *
 * @author IceRain
 * @since 2023/04/12
 */
@Service(Service.Level.APP)
@State(
        name = KeyConstant.TOOL_WINDOW_KEY_SCRIPT,
        storages = @Storage(KeyConstant.CONFIG_STATE_FILENAME)
)
public final class ToolWindowScriptState implements PersistentStateComponent<ToolWindowScriptState> {

    public List<ScriptFile> scriptFiles = new ArrayList<>();

    /**
     * 获取实例
     *
     * @return 当前实例
     */
    public static ToolWindowScriptState getInstance() {
        return ApplicationManager.getApplication().getService(ToolWindowScriptState.class);
    }

    @Override
    public @NotNull ToolWindowScriptState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ToolWindowScriptState toolWindowScriptState) {
        XmlSerializerUtil.copyBean(toolWindowScriptState, this);
    }

}
