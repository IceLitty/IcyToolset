package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowSymbol {

    JPanel panelMain;
    JTextArea textareaSymbol;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowSymbol(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }

    public JPanel getContent() {
        return this.panelMain;
    }

    @SuppressWarnings("unused")
    public Project getCurrentProject() {
        return this.project;
    }

    @SuppressWarnings("unused")
    public ToolWindow getCurrentToolWindow() {
        return this.toolWindow;
    }

    private void createUIComponents() {
    }

}
