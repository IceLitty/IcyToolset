package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.service.ToolWindowFormatEditorService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowFormat {

    JPanel panelMain;
    EditorEx editor;
    JComponent textareaFormat;
    JButton buttonFormatDo;
    JButton buttonFormatUndo;
    JPanel panelTextareaFormat;

    ADocumentListener aDocumentListener = null;

    private final Project project;
    private final ToolWindow toolWindow;

    public ToolWindowFormat(Project project, ToolWindow toolWindow) {
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
        if (this.aDocumentListener == null) {
            this.aDocumentListener = new ADocumentListener();
        }
        ToolWindowFormatEditorService toolWindowFormatEditorService = this.project.getService(ToolWindowFormatEditorService.class);
        FileType plainTextFileType = FileTypeManager.getInstance().getFileTypeByExtension("TXT");
        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("format.txt", plainTextFileType, "", 0, true);
        Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
        if (document == null) {
            document = EditorFactory.getInstance().createDocument("");
        }
        this.editor = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
        this.editor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
        this.editor.getDocument().addDocumentListener(this.aDocumentListener);
        this.textareaFormat = this.editor.getComponent();
        toolWindowFormatEditorService.setEditor(this.editor);
    }

    /**
     * 使用指定格式重新创建Editor控件
     *
     * @param fileSuffix 文件后缀
     */
    private void useFormatToCreateEditor(String fileSuffix) {
        ToolWindowFormatEditorService toolWindowFormatEditorService = this.project.getService(ToolWindowFormatEditorService.class);
        toolWindowFormatEditorService.setEditor(null);
        String text = this.editor.getDocument().getText();
        this.panelTextareaFormat.remove(this.textareaFormat);
        if (this.editor != null && !this.editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(this.editor);
        }
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(fileSuffix);
        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("format." + fileSuffix, fileType, text, 0, true);
        Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
        if (document == null) {
            document = EditorFactory.getInstance().createDocument("");
        }
        this.editor = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
        this.editor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
        this.editor.getDocument().addDocumentListener(this.aDocumentListener);
        this.textareaFormat = this.editor.getComponent();
        this.panelTextareaFormat.add(this.textareaFormat, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, new Dimension(-1, -1), new Dimension(-1, -1), new Dimension(-1, -1), 0, true));
        this.panelTextareaFormat.validate();
        this.panelTextareaFormat.repaint();
        toolWindowFormatEditorService.setEditor(this.editor);
        IdeFocusManager.getInstance(this.project).requestFocus(this.editor.getContentComponent(), true);
        try {
            this.editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 1));
        } catch (Exception ignored) {}
    }

    class ADocumentListener implements DocumentListener {
        private String lastFormat = "null";
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (editor.isDisposed()) {
                    return;
                }
                String text = editor.getDocument().getText();
                String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
                if (tmp.isEmpty()) {
                    return;
                }
                tmp = tmp.substring(0, 1);
                if ("{".equals(tmp) || "[".equals(tmp)) {
                    if (!"json".equals(lastFormat)) {
                        useFormatToCreateEditor("json");
                        lastFormat = "json";
                    }
                } else if ("<".equals(tmp)) {
                    if (!"xml".equals(lastFormat)) {
                        useFormatToCreateEditor("xml");
                        lastFormat = "xml";
                    }
                }
            });
        }
    }

}
