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

    private final MainForm mainForm;
    private final AtomicBoolean injectedNashorn = new AtomicBoolean(false);

    public MainFormScriptFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.textareaScriptSource.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (mainForm.checkScriptAutoRun.isSelected()) {
                    eval();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (mainForm.checkScriptAutoRun.isSelected()) {
                    eval();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.mainForm.radioScriptJavascript.addActionListener(this::radioChanged);
        this.mainForm.radioScriptPython.addActionListener(this::radioChanged);
        this.mainForm.radioScriptLua.addActionListener(this::radioChanged);
        this.mainForm.radioScriptGroovy.addActionListener(this::radioChanged);
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollScriptSource, this.mainForm.scrollScriptResult);
        this.mainForm.scrollScriptSource.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptSource.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        injectNashorn();
    }

    private void radioChanged(ActionEvent e) {
        if (((JRadioButton) e.getSource()).isSelected()) {
            if (this.mainForm.checkScriptAutoRun.isSelected()) {
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
            String script = this.mainForm.textareaScriptSource.getText();
            if (this.mainForm.radioScriptJavascript.isSelected()) {
                this.mainForm.textareaScriptResult.setText(String.valueOf(ScriptUtil.getJsEngine().eval(script)));
            } else if (this.mainForm.radioScriptLua.isSelected()) {
                this.mainForm.textareaScriptResult.setText(String.valueOf(ScriptUtil.getLuaEngine().eval(script)));
            } else if (this.mainForm.radioScriptGroovy.isSelected()) {
                this.mainForm.textareaScriptResult.setText(String.valueOf(ScriptUtil.getGroovyEngine().eval(script)));
            } else if (this.mainForm.radioScriptPython.isSelected()) {
                this.mainForm.textareaScriptResult.setText(String.valueOf(ScriptUtil.getPythonEngine().eval(script)));
            } else {
                NotificationUtil.warning(MessageUtil.getMessage("script.not.select.type"));
            }
        } catch (Exception ex) {
            this.mainForm.textareaScriptResult.setText(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
        }
    }

}
