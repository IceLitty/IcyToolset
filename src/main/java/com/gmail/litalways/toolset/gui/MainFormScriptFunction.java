package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.state.ScriptFile;
import com.gmail.litalways.toolset.state.ToolWindowScriptState;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.python.jsr223.PyScriptEngineFactory;

import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormScriptFunction {

    @SuppressWarnings("FieldCanBeLocal")
    private final ToolWindowScript component;
    private final AtomicBoolean injectedNashorn = new AtomicBoolean(false);

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
        injectNashorn();
    }

    private void injectNashorn() {
        if (injectedNashorn.compareAndSet(false, true)) {
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
                    NashornScriptEngineFactory nashornScriptEngineFactory = new NashornScriptEngineFactory();
                    manager.registerEngineName("js", nashornScriptEngineFactory);
                    LuaScriptEngineFactory luaScriptEngineFactory = new LuaScriptEngineFactory();
                    manager.registerEngineName("lua", luaScriptEngineFactory);
                    PyScriptEngineFactory pyScriptEngineFactory = new PyScriptEngineFactory();
                    manager.registerEngineName("python", pyScriptEngineFactory);
                }
            } catch (Exception e) {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.nashorn.inject.fail"), e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
            }
        }
    }

}
