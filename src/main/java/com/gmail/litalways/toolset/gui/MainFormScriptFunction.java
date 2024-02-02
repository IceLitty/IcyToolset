package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.state.ScriptFile;
import com.gmail.litalways.toolset.state.ToolWindowScriptState;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.python.jsr223.PyScriptEngineFactory;

import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormScriptFunction {

    @SuppressWarnings("FieldCanBeLocal")
    private final ToolWindowScript component;

    public MainFormScriptFunction(ToolWindowScript component) {
        this.component = component;
        this.component.radioScriptJavascript.addActionListener(this.component::radioChanged);
        this.component.radioScriptPython.addActionListener(this.component::radioChanged);
        this.component.radioScriptLua.addActionListener(this.component::radioChanged);
        this.component.radioScriptGroovy.addActionListener(this.component::radioChanged);
        this.component.textScriptFilename.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ScriptFile selectedValue = component.scriptList.getSelectedValue();
                if (selectedValue != null) {
                    selectedValue.setFileName(component.textScriptFilename.getText());
                    ToolWindowScriptState.getInstance().scriptFiles.removeIf(s -> s == selectedValue);
                    ToolWindowScriptState.getInstance().scriptFiles.add(selectedValue);
                    component.scriptModel.fireListDataChanged();
                }
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ScriptFile selectedValue = component.scriptList.getSelectedValue();
                if (selectedValue != null) {
                    selectedValue.setFileName(component.textScriptFilename.getText());
                    ToolWindowScriptState.getInstance().scriptFiles.removeIf(s -> s == selectedValue);
                    ToolWindowScriptState.getInstance().scriptFiles.add(selectedValue);
                    component.scriptModel.fireListDataChanged();
                }
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });
        this.component.syncListener = new ScrollbarSyncListener(this.component.textareaScriptSourceEditor.getScrollPane(), this.component.scrollScriptResult);
        this.component.textareaScriptSourceEditor.getScrollPane().getVerticalScrollBar().addAdjustmentListener(this.component.syncListener);
        this.component.textareaScriptSourceEditor.getScrollPane().getHorizontalScrollBar().addAdjustmentListener(this.component.syncListener);
        ProgressManager.getInstance().run(new Task.Backgroundable(this.component.getCurrentProject(), MessageUtil.getMessage("script.tip.nashorn.injecting")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                injectNashorn();
            }
        });
    }

    private void injectNashorn() {
        if (this.component.injectedNashorn.compareAndSet(0, 1)) {
            try {
                Field[] declaredFields = ScriptUtil.class.getDeclaredFields();
                ScriptEngineManager manager = null;
                for (Field f : declaredFields) {
                    if (f.getType() == ScriptEngineManager.class) {
                        f.setAccessible(true);
                        manager = (ScriptEngineManager) f.get(null);
                    }
                }
                if (manager != null) {
                    // JavaScript
                    NashornScriptEngineFactory nashornScriptEngineFactory = new NashornScriptEngineFactory();
                    manager.registerEngineName("js", nashornScriptEngineFactory);
                    // Lua
                    LuaScriptEngineFactory luaScriptEngineFactory = new LuaScriptEngineFactory();
                    manager.registerEngineName("lua", luaScriptEngineFactory);
                    // Python
                    PyScriptEngineFactory pyScriptEngineFactory = new PyScriptEngineFactory();
                    manager.registerEngineName("python", pyScriptEngineFactory);
                    // Groovy 由窗体类动态加载实例，支持热部署项目输出+依赖
                    ScriptUtil.getGroovyEngine();
                    this.component.injectedNashorn.set(2);
                    return;
                }
                this.component.injectedNashorn.set(3);
            } catch (Exception e) {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.nashorn.inject.fail"), e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                this.component.injectedNashorn.set(3);
            }
        }
    }

}
