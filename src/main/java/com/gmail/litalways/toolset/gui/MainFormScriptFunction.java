package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.python.jsr223.PyScriptEngineFactory;

import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormScriptFunction {

    private final ToolWindowScript component;
    private final AtomicBoolean injectedNashorn = new AtomicBoolean(false);

    public MainFormScriptFunction(ToolWindowScript component) {
        this.component = component;
        this.component.textareaScriptSource.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (component.checkScriptAutoRun.isSelected()) {
                    eval();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (component.checkScriptAutoRun.isSelected()) {
                    eval();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.component.radioScriptJavascript.addActionListener(this::radioChanged);
        this.component.radioScriptPython.addActionListener(this::radioChanged);
        this.component.radioScriptLua.addActionListener(this::radioChanged);
        this.component.radioScriptGroovy.addActionListener(this::radioChanged);
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.component.scrollScriptSource, this.component.scrollScriptResult);
        this.component.scrollScriptSource.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptSource.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        injectNashorn();
    }

    private void radioChanged(ActionEvent e) {
        if (((JRadioButton) e.getSource()).isSelected()) {
            if (this.component.checkScriptAutoRun.isSelected()) {
                eval();
            }
        }
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
                NotificationUtil.warning(MessageUtil.getMessage("script.nashorn.inject.fail"), e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
            }
        }
    }

    private void eval() {
        try {
            String script = this.component.textareaScriptSource.getText();
            if (this.component.radioScriptJavascript.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getJsEngine().eval(script)));
            } else if (this.component.radioScriptLua.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getLuaEngine().eval(script)));
            } else if (this.component.radioScriptGroovy.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getGroovyEngine().eval(script)));
            } else if (this.component.radioScriptPython.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getPythonEngine().eval(script)));
            } else {
                NotificationUtil.warning(MessageUtil.getMessage("script.not.select.type"));
            }
        } catch (Exception ex) {
            this.component.textareaScriptResult.setText(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
        }
    }

}
