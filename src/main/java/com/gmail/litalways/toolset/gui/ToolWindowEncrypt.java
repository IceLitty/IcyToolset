package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowEncrypt {

    JPanel panelMain;
    JTabbedPane tabEncrypt;
    JComboBox<String> selectEncryptHashEncoding;
    JComboBox<String> selectEncryptHashType;
    JButton buttonEncryptHashFile;
    JButton buttonEncryptHashText;
    JCheckBox checkEncryptHashLine;
    JComboBox<String> selectEncryptHashOutputType;
    JButton buttonEncryptHashClean;
    JScrollPane scrollEncryptHashText;
    JTextArea textareaEncryptHashText;
    TextFieldWithBrowseButton fileEncryptHashFile;
    JTextField textEncryptHashKey;
    JButton buttonEncryptHashGenerateKey;
    JScrollPane scrollEncryptHashResult;
    JTextArea textareaEncryptHashResult;
    JTextField textEncryptHashAssert;
    TextFieldWithBrowseButton fileEncryptHashAsserts;
    JTextField textEncryptAsymmetricPublicKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPublicKey;
    JTextField textEncryptAsymmetricPrivateKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPrivateKey;
    JButton buttonEncryptAsymmetricEncryptWithPublicKey;
    JButton buttonEncryptAsymmetricDecryptWithPrivateKey;
    JButton buttonEncryptAsymmetricEncryptWithPrivateKey;
    JButton buttonEncryptAsymmetricDecryptWithPublicKey;
    JComboBox<String> selectEncryptAsymmetricEncoding;
    JComboBox<String> selectEncryptAsymmetricType;
    JButton buttonEncryptAsymmetricGenerateKey;
    JButton buttonEncryptAsymmetricClean;
    JScrollPane scrollEncryptAsymmetricEncrypted;
    JTextArea textareaEncryptAsymmetricEncrypted;
    JScrollPane scrollEncryptAsymmetricDecrypted;
    JTextArea textareaEncryptAsymmetricDecrypted;
    JComboBox<String> selectEncryptSymmetricType;
    JComboBox<String> selectEncryptSymmetricMode;
    JComboBox<String> selectEncryptSymmetricPadding;
    JComboBox<String> selectEncryptSymmetricOutputType;
    JTextField textEncryptSymmetricKey;
    JTextField textEncryptSymmetricIV;
    JTextField textEncryptSymmetricSalt;
    JButton buttonEncryptSymmetricEncrypt;
    JButton buttonEncryptSymmetricDecrypt;
    JComboBox<String> selectEncryptSymmetricEncoding;
    JButton buttonEncryptSymmetricClean;
    JScrollPane scrollEncryptSymmetricDecrypted;
    JTextArea textareaEncryptSymmetricDecrypted;
    JScrollPane scrollEncryptSymmetricEncrypted;
    JTextArea textareaEncryptSymmetricEncrypted;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowEncrypt(Project project, ToolWindow toolWindow) {
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
