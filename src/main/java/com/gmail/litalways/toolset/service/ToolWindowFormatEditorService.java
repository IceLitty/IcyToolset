package com.gmail.litalways.toolset.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;

/**
 * @author IceRain
 * @since 2023/04/12
 */
@Service(Service.Level.PROJECT)
public final class ToolWindowFormatEditorService {

    private final Project project;
    @Setter
    @Getter
    private EditorEx editor;

    public ToolWindowFormatEditorService(Project project) {
        this.project = project;
    }

    public void disposed() {
        if (this.editor != null && !this.editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(this.editor);
        }
    }

}
