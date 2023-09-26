package com.gmail.litalways.toolset.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;

/**
 * @author IceRain
 * @since 2023/09/20
 */
@Service(Service.Level.PROJECT)
public final class ToolWindowEncryptEditorService {

    private final Project project;
    private EditorEx[] editors;

    public ToolWindowEncryptEditorService(Project project) {
        this.project = project;
    }

    public EditorEx[] getEditors() {
        return editors;
    }

    public void setEditors(EditorEx... editors) {
        this.editors = editors;
    }

    public void disposed() {
        if (this.editors != null && this.editors.length != 0) {
            for (int i = 0; i < this.editors.length; i++) {
                if (!this.editors[i].isDisposed()) {
                    EditorFactory.getInstance().releaseEditor(this.editors[i]);
                }
            }
        }
    }

}
