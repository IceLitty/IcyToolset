package com.gmail.litalways.toolset.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author IceRain
 * @since 2023/04/14
 */
@SuppressWarnings("ComponentNotRegistered")
public class ProjectViewFindByUsedForCopyBeanAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: 寻找该右键出来文件的实例是否被各种BeanUtil操作过，并显示find usages窗口
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        boolean visible = psiFile != null && !psiFile.isDirectory() && psiFile.getName().endsWith(".java");
        e.getPresentation().setEnabledAndVisible(visible);
    }

}
