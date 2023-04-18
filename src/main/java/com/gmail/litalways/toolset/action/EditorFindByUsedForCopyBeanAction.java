package com.gmail.litalways.toolset.action;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * 在Editor控件显示编辑提示，用以寻找该类/实例是否被各类BeanUtil进行拷贝
 *
 * @author IceRain
 * @since 2023/04/17
 */
public class EditorFindByUsedForCopyBeanAction implements IntentionAction {

    /**
     * 操作提示
     */
    @Override
    public @IntentionName @NotNull String getText() {
        return "SAMPLE TEXT";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "SAMPLE NAME";
    }

    /**
     * 是否显示提示
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    /**
     * 预览操作或执行操作
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        System.out.println("INVOKE ACTION");
        System.out.println("Project " + project); // Project(name=untitled1, containerState=COMPONENT_CREATED, componentStore=C:\Users\IceRain\IdeaProjects\ untitled1)
        System.out.println("Editor " + editor.getClass().getName() + " " + editor); // {} or EditorImpl[file://C:/Users/IceRain/IdeaProjects/untitled1/src/Main.java]
        System.out.println("Psi " + psiFile); // PsiJavaFile:Main.java
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    /**
     * 代码生成预览
     */
    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return IntentionAction.super.generatePreview(project, editor, file);
    }

}
