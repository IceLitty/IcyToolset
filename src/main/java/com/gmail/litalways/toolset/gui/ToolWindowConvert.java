package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.filter.FileSizeTextFormat;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.text.NumberFormat;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowConvert {

    JPanel panelMain;
    JTabbedPane tabConvert;
    JRadioButton radioConvertCommonBase64;
    JRadioButton radioConvertCommonHex;
    JRadioButton radioConvertCommonHtml;
    JRadioButton radioConvertCommonUnicode;
    JCheckBox checkConvertCommonLine;
    JCheckBox checkConvertCommonAuto;
    JRadioButton radioConvertCommonUriComponent;
    JComboBox<String> selectConvertCommonCharset;
    JRadioButton radioConvertCommonJson;
    JRadioButton radioConvertCommonTime;
    JScrollPane scrollConvertCommonDecoded;
    JTextArea textareaConvertCommonDecoded;
    JScrollPane scrollConvertCommonEncoded;
    JTextArea textareaConvertCommonEncoded;
    JButton buttonConvertCommonEncode;
    JButton buttonConvertCommonDecode;
    JButton buttonConvertCommonClean;
    TextFieldWithBrowseButton fileConvertImgBase64Path;
    JComboBox<String> selectConvertImgBase64Charset;
    JButton buttonConvertImgBase64Decode;
    JButton buttonConvertImgBase64Encode;
    JButton buttonConvertImgBase64Clean;
    JTextArea textareaConvertImgBase64;
    TextFieldWithBrowseButton fileConvertSplitterPath;
    JFormattedTextField textConvertSplitterCacheSize;
    JFormattedTextField textConvertSplitterCount;
    JFormattedTextField textConvertSplitterSize;
    JButton buttonConvertSplitterRun;
    JCheckBox checkConvertSplitterLineFlag;
    JButton buttonConvertSplitterClear;
    JTextArea textAreaConvertSplitterOutput;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowConvert(Project project, ToolWindow toolWindow) {
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
        this.textConvertSplitterCacheSize = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.textConvertSplitterCount = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.textConvertSplitterSize = new JFormattedTextField(FileSizeTextFormat.getInstance());
    }

}
