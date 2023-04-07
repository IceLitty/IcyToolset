package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.text.NumberFormat;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowQRCode {

    JPanel panelMain;
    JButton buttonZxingToFile;
    JFormattedTextField textZxingWidth;
    JFormattedTextField textZxingHeight;
    JComboBox<String> selectZxingEncoding;
    JComboBox<String> selectZxingErrorCorrection;
    JButton buttonZxingClean;
    JTextArea textareaZxingText;
    JRadioButton radioZxingQr;
    TextFieldWithBrowseButton fileZxingFromFile;
    JRadioButton radioZxingBar;
    JRadioButton radioZxingMatrix;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowQRCode(Project project, ToolWindow toolWindow) {
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
        this.textZxingWidth = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.textZxingHeight = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }

}
