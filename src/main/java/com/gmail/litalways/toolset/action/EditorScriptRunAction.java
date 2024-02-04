package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.gui.ToolWindowScript;
import com.gmail.litalways.toolset.service.ToolWindowScriptEditorService;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @author IceRain
 * @since 2024/02/02
 */
@Slf4j
public class EditorScriptRunAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            NotificationUtil.warning(MessageUtil.getMessage("script.tip.shortcut.run.get.project.null"));
        } else {
            ToolWindowScriptEditorService service = project.getService(ToolWindowScriptEditorService.class);
            if (service == null) {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.shortcut.run.get.service.null"));
            } else {
                ToolWindowScript scriptUi = service.getScriptUi();
                if (scriptUi != null) {
                    scriptUi.eval();
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            log.warn(MessageUtil.getMessage("script.tip.shortcut.run.get.project.null"));
            e.getPresentation().setEnabledAndVisible(false);
        } else {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            ToolWindowScriptEditorService service = project.getService(ToolWindowScriptEditorService.class);
            if (service == null) {
                log.warn(MessageUtil.getMessage("script.tip.shortcut.run.get.service.null"));
                e.getPresentation().setEnabledAndVisible(false);
            } else {
                boolean nowScriptEditor = service.isNowScriptEditor(editor);
                e.getPresentation().setEnabledAndVisible(nowScriptEditor);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
