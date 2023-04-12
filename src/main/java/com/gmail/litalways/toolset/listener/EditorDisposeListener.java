package com.gmail.litalways.toolset.listener;

import com.gmail.litalways.toolset.service.ToolWindowFormatEditorService;
import com.gmail.litalways.toolset.service.ToolWindowScriptEditorService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * @author IceRain
 * @since 2023/04/11
 */
public class EditorDisposeListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        ToolWindowScriptEditorService toolWindowScriptEditorService = project.getService(ToolWindowScriptEditorService.class);
        toolWindowScriptEditorService.disposed();
        ToolWindowFormatEditorService toolWindowFormatEditorService = project.getService(ToolWindowFormatEditorService.class);
        toolWindowFormatEditorService.disposed();
    }

    @Override
    public void projectClosingBeforeSave(@NotNull Project project) {
    }

}
