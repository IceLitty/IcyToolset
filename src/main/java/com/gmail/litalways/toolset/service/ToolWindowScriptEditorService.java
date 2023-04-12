package com.gmail.litalways.toolset.service;

import com.gmail.litalways.toolset.state.ScriptFile;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

/**
 * @author IceRain
 * @since 2023/04/11
 */
@Service(Service.Level.PROJECT)
public final class ToolWindowScriptEditorService {

    private final Project project;
    private EditorEx editor;
    private ScriptFile lastScriptFile = null;

    public ToolWindowScriptEditorService(Project project) {
        this.project = project;
    }

    public EditorEx newEditor(String fileName, FileType fileType, String content) {
        lastScriptFile = null;
        EditorFactory editorFactory = EditorFactory.getInstance();
        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText(fileName, fileType, content, 0, true);
        Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
        if (document == null) {
            document = EditorFactory.getInstance().createDocument(content);
        }
        this.editor = (EditorEx) editorFactory.createEditor(document, this.project);
//        EditorSettings editorSettings = this.editor.getSettings();
//        editorSettings.setVirtualSpace(false);
//        editorSettings.setLineMarkerAreaShown(false);
//        editorSettings.setIndentGuidesShown(false);
//        editorSettings.setLineNumbersShown(false);
//        editorSettings.setFoldingOutlineShown(false);
//        editorSettings.setAdditionalColumnsCount(3);
//        editorSettings.setAdditionalLinesCount(3);
//        editorSettings.setCaretRowShown(false);
        this.editor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
        return this.editor;
    }

    public void disposed() {
        if (this.editor != null && !this.editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(this.editor);
        }
    }

    public ScriptFile getLastScriptFile() {
        return lastScriptFile;
    }

    public void setLastScriptFile(ScriptFile lastScriptFile) {
        this.lastScriptFile = lastScriptFile;
    }

}