package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.filter.LongNumberTextFormat;
import com.gmail.litalways.toolset.service.ToolWindowEncryptEditorService;
import com.gmail.litalways.toolset.util.*;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author IceRain
 * @since 2023/04/07
 */
@Slf4j
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
    JButton buttonEncryptHashOpenDirectory;
    JTextField textEncryptHashKey;
    JButton buttonEncryptHashGenerateKey;
    JScrollPane scrollEncryptHashResult;
    JTextArea textareaEncryptHashResult;
    JTextField textEncryptHashAssert;
    TextFieldWithBrowseButton fileEncryptHashAsserts;
    JButton buttonEncryptHashAssertsOpenDirectory;
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
    JLabel jwtVerifyStatusIcon;
    JLabel jwtVerifyStatus;
    JComponent jwtPublicKeyEditor;
    JComponent jwtPrivateKeyEditor;
    JComponent jwtDecodedEditor;
    JComponent jwtEncodedEditor;
    EditorEx jwtPublicKeyEditorEx;
    EditorEx jwtPrivateKeyEditorEx;
    EditorEx jwtDecodedEditorEx;
    EditorEx jwtEncodedEditorEx;
    MainFormEncryptJWTFunction.ADocumentListener jwtPublicKeyDocumentListener;
    MainFormEncryptJWTFunction.ADocumentListener jwtPrivateKeyDocumentListener;
    MainFormEncryptJWTFunction.ADocumentListener jwtDecodedDocumentListener;
    MainFormEncryptJWTFunction.ADocumentListener jwtEncodedDocumentListener;
    JButton jwtEncodeBtn;
    JButton jwtDecodeBtn;
    JCheckBox jwtAutoRun;
    JComboBox<String> jwtSelectAlgorithm;
    JFormattedTextField jwtIssuedAt;
    JFormattedTextField jwtNotBefore;
    JFormattedTextField jwtExpirationTime;
    JTextField jwtAudience;
    JTextField jwtIssuer;
    JTextField jwtSubject;
    JTextField jwtID;
    JButton jwtCleanBtn;
    JButton jwtPemJwkSwitchBtn;

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
        this.jwtNotBefore = new JFormattedTextField(LongNumberTextFormat.getInstance());
        this.jwtIssuedAt = new JFormattedTextField(LongNumberTextFormat.getInstance());
        this.jwtExpirationTime = new JFormattedTextField(LongNumberTextFormat.getInstance());
        ToolWindowEncryptEditorService editorService = this.project.getService(ToolWindowEncryptEditorService.class);
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtEncoded.txt", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtEncodedEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtEncodedEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtEncodedEditor = this.jwtEncodedEditorEx.getComponent();
            try {
                Method getSettings = Editor.class.getDeclaredMethod("getSettings");
                EditorSettings editorSettings = (EditorSettings) getSettings.invoke(this.jwtEncodedEditorEx);
                editorSettings.setUseSoftWraps(true);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                NotificationUtil.error(MessageUtil.getMessage("encrypt.jwt.tip.init.soft.warp.invoke.error"));
            }
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtDecoded.json", JsonFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtDecodedEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtDecodedEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtDecodedEditor = this.jwtDecodedEditorEx.getComponent();
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtJwkPublicKey.json", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtPublicKeyEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtPublicKeyEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtPublicKeyEditor = this.jwtPublicKeyEditorEx.getComponent();
            this.jwtPublicKeyEditor.setToolTipText(MessageUtil.getMessage("encrypt.jwt.editor.public.tooltip"));
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtJwkPrivateKey.json", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtPrivateKeyEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtPrivateKeyEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtPrivateKeyEditor = this.jwtPrivateKeyEditorEx.getComponent();
            this.jwtPrivateKeyEditor.setToolTipText(MessageUtil.getMessage("encrypt.jwt.editor.private.tooltip"));
        }
        editorService.setEditors(this.jwtEncodedEditorEx, this.jwtDecodedEditorEx, this.jwtPublicKeyEditorEx, this.jwtPrivateKeyEditorEx);
    }

}
