package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.filter.FileSizeTextFormat;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.text.NumberFormat;

/**
 * @author IceRain
 * @since 2022/01/19
 */
@Deprecated
public class MainForm {

    JPanel panelMain;
    JTabbedPane tabMain;
    // 转换
    JTabbedPane tabConvert;
    // 转换 - 通用
    JComboBox<String> selectConvertCommonCharset;
    JRadioButton radioConvertCommonBase64;
    JRadioButton radioConvertCommonHex;
    JRadioButton radioConvertCommonHtml;
    JRadioButton radioConvertCommonUnicode;
    JRadioButton radioConvertCommonUriComponent;
    JRadioButton radioConvertCommonJson;
    JRadioButton radioConvertCommonTime;
    JTextArea textareaConvertCommonDecoded;
    JTextArea textareaConvertCommonEncoded;
    JButton buttonConvertCommonEncode;
    JButton buttonConvertCommonDecode;
    JCheckBox checkConvertCommonLine;
    JCheckBox checkConvertCommonAuto;
    JScrollPane scrollConvertCommonDecoded;
    JScrollPane scrollConvertCommonEncoded;
    JButton buttonConvertCommonClean;
    // 转换 - 文件
    JTextArea textareaConvertImgBase64;
    TextFieldWithBrowseButton fileConvertImgBase64Path;
    JComboBox<String> selectConvertImgBase64Charset;
    JButton buttonConvertImgBase64Decode;
    JButton buttonConvertImgBase64Encode;
    JButton buttonConvertImgBase64Clean;
    // 转换 - 大文本分割
    JTextArea textAreaConvertSplitterOutput;
    TextFieldWithBrowseButton fileConvertSplitterPath;
    JFormattedTextField textConvertSplitterCacheSize;
    JCheckBox checkConvertSplitterLineFlag;
    JFormattedTextField textConvertSplitterCount;
    JFormattedTextField textConvertSplitterSize;
    JButton buttonConvertSplitterRun;
    // 加解密
    JTabbedPane tabEncrypt;
    // 加解密 - 摘要计算
    TextFieldWithBrowseButton fileEncryptHashFile;
    JButton buttonEncryptHashFile;
    JButton buttonEncryptHashText;
    JButton buttonEncryptHashClean;
    JTextArea textareaEncryptHashText;
    JTextArea textareaEncryptHashResult;
    JComboBox<String> selectEncryptHashEncoding;
    JComboBox<String> selectEncryptHashType;
    JTextField textEncryptHashKey;
    JButton buttonEncryptHashGenerateKey;
    JTextField textEncryptHashAssert;
    TextFieldWithBrowseButton fileEncryptHashAsserts;
    JCheckBox checkEncryptHashLine;
    JComboBox<String> selectEncryptHashOutputType;
    JScrollPane scrollEncryptHashText;
    JScrollPane scrollEncryptHashResult;
    // 加解密 - 非对称
    JTextField textEncryptAsymmetricPublicKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPublicKey;
    JTextField textEncryptAsymmetricPrivateKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPrivateKey;
    JTextArea textareaEncryptAsymmetricDecrypted;
    JTextArea textareaEncryptAsymmetricEncrypted;
    JButton buttonEncryptAsymmetricEncryptWithPublicKey;
    JButton buttonEncryptAsymmetricDecryptWithPrivateKey;
    JButton buttonEncryptAsymmetricEncryptWithPrivateKey;
    JButton buttonEncryptAsymmetricDecryptWithPublicKey;
    JButton buttonEncryptAsymmetricGenerateKey;
    JButton buttonEncryptAsymmetricClean;
    JComboBox<String> selectEncryptAsymmetricEncoding;
    JComboBox<String> selectEncryptAsymmetricType;
    JScrollPane scrollEncryptAsymmetricDecrypted;
    JScrollPane scrollEncryptAsymmetricEncrypted;
    // 加解密 - 对称
    JComboBox<String> selectEncryptSymmetricType;
    JComboBox<String> selectEncryptSymmetricMode;
    JComboBox<String> selectEncryptSymmetricPadding;
    JTextField textEncryptSymmetricKey;
    JTextField textEncryptSymmetricIV;
    JComboBox<String> selectEncryptSymmetricOutputType;
    JComboBox<String> selectEncryptSymmetricEncoding;
    JButton buttonEncryptSymmetricEncrypt;
    JButton buttonEncryptSymmetricDecrypt;
    JButton buttonEncryptSymmetricClean;
    JTextArea textareaEncryptSymmetricDecrypted;
    JTextArea textareaEncryptSymmetricEncrypted;
    JScrollPane scrollEncryptSymmetricDecrypted;
    JScrollPane scrollEncryptSymmetricEncrypted;
    // 条码转换
    JRadioButton radioZxingQr;
    JRadioButton radioZxingBar;
    JRadioButton radioZxingMatrix;
    JTextArea textareaZxingText;
    TextFieldWithBrowseButton fileZxingFromFile;
    JButton buttonZxingToFile;
    JButton buttonZxingClean;
    JFormattedTextField textZxingWidth;
    JFormattedTextField textZxingHeight;
    JComboBox<String> selectZxingEncoding;
    JComboBox<String> selectZxingErrorCorrection;
    // 格式化
    JTextArea textareaFormat;
    JButton buttonFormatDo;
    JButton buttonFormatUndo;
    // 脚本
    JTextArea textareaScriptSource;
    JTextArea textareaScriptResult;
    JScrollPane scrollScriptSource;
    JScrollPane scrollScriptResult;
    JCheckBox checkScriptAutoRun;
    JRadioButton radioScriptJavascript;
    JRadioButton radioScriptPython;
    JRadioButton radioScriptLua;
    JRadioButton radioScriptGroovy;
    // 标志
    JTextArea textareaSymbol;
    // 其他
    JButton buttonAboutEncoding;
    JButton buttonAboutGC;
    JButton buttonAboutGenHashRun;
    TextFieldWithBrowseButton fileAboutGenHashPath;
    JComboBox<String> selectAboutGenHashType;
    JTextField textAboutGenHashSuffix;
    JCheckBox checkAboutGenHashPom;
    JTextField textAboutGenHashPathFilter;
    JTextField textAboutGenHashFileFilter;
    JTextField textEncryptSymmetricSalt;
    JButton buttonConvertSplitterClear;

    private final Project project;
    private final ToolWindow toolWindow;

    public MainForm(Project project, ToolWindow toolWindow) {
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
        this.textZxingWidth = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.textZxingHeight = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }

}
