package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowOther {

    JPanel panelMain;
    JButton buttonAboutGenHashRun;
    TextFieldWithBrowseButton fileAboutGenHashPath;
    JComboBox<String> selectAboutGenHashType;
    JTextField textAboutGenHashSuffix;
    JTextField textAboutGenHashPathFilter;
    JTextField textAboutGenHashFileFilter;
    JCheckBox checkAboutGenHashPom;
    JButton buttonAboutEncoding;
    JButton buttonAboutGC;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowOther(Project project, ToolWindow toolWindow) {
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
