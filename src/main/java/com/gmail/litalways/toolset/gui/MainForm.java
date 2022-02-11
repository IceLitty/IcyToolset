package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.text.NumberFormat;

/**
 * @author IceRain
 * @since 2022/01/19
 */
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
    // 转换 - 文件
    JTextArea textareaConvertImgBase64;
    TextFieldWithBrowseButton fileConvertImgBase64Path;
    JComboBox<String> selectConvertImgBase64Charset;
    JButton buttonConvertImgBase64Decode;
    JButton buttonConvertImgBase64Encode;
    // 加解密
    JTabbedPane tabEncrypt;
    // 加解密 - 摘要计算
    TextFieldWithBrowseButton fileEncryptHashFile;
    JButton buttonEncryptHashFile;
    JButton buttonEncryptHashText;
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
    JFormattedTextField textZxingWidth;
    JFormattedTextField textZxingHeight;
    JComboBox<String> selectZxingEncoding;
    JComboBox<String> selectZxingErrorCorrection;
    // 格式化
    JTextArea textareaFormat;
    JButton buttonFormat;
    // 脚本
    JTextArea textareaScriptSource;
    JTextArea textareaScriptResult;
    JScrollPane scrollScriptSource;
    JScrollPane scrollScriptResult;
    // 标志
    JTextArea textareaSymbol;
    // 其他
    JButton buttonAboutEncoding;

    private final ToolWindow toolWindow;

    public MainForm(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public JPanel getContent() {
        return this.panelMain;
    }

    private void createUIComponents() {
        this.textZxingWidth = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.textZxingHeight = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }

}
