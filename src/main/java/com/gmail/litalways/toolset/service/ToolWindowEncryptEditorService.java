package com.gmail.litalways.toolset.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import lombok.Getter;

/**
 * @author IceRain
 * @since 2023/09/20
 */
@Service(Service.Level.PROJECT)
public final class ToolWindowEncryptEditorService {

    private final Project project;
    @Getter
    private EditorEx[] editors;

    public ToolWindowEncryptEditorService(Project project) {
        this.project = project;
    }

    public void setEditors(EditorEx... editors) {
        this.editors = editors;
    }

    public void disposed() {
        if (this.editors != null) {
            for (EditorEx editor : this.editors) {
                if (!editor.isDisposed()) {
                    EditorFactory.getInstance().releaseEditor(editor);
                }
            }
        }
    }

}
