package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.intellij.ide.highlighter.JavaFileType;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormScriptFunction {

    private final MainForm mainForm;

    public MainFormScriptFunction(MainForm mainForm) {
        this.mainForm = mainForm;
        this.mainForm.textareaScriptSource.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                eval();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                eval();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.mainForm.scrollScriptSource, this.mainForm.scrollScriptResult);
        this.mainForm.scrollScriptSource.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptSource.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.mainForm.scrollScriptResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
    }

    private void eval() {
        try {
            this.mainForm.textareaScriptResult.setText(String.valueOf(ScriptUtil.eval(this.mainForm.textareaScriptSource.getText())));
        } catch (Exception ex) {
            this.mainForm.textareaScriptResult.setText(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
        }
    }

}
