package com.gmail.litalways.toolset.action;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.find.FindManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 在Editor控件显示编辑提示，用以寻找该类/实例是否被各类BeanUtil进行拷贝
 * TODO 这个还是原型，ProjectView那个已经实现好了，这个考虑改成不搜索实例只搜索类型？还是识别选中的identifier的类型然后分别处理？
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
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) {
            return;
        }
//        FindManager.getInstance(project).findUsages(psiElement, true);
        PsiType psiType = ((PsiLocalVariable) psiElement.getParent()).getType();
        PsiClass psiClass = ((PsiClassType) psiType).resolve();
        PsiMethodMember psiMethodMember = null;
        for (PsiMethod psiMethod : psiClass.getAllMethods()) {
            if (psiMethod.getName().startsWith("set")) {
                psiMethodMember = new PsiMethodMember(psiMethod);
                break;
            }
        }
        if (psiMethodMember == null) {
            return;
        }
//        Query<PsiReference> psiReferences = MethodReferencesSearch.search(psiMethodMember.getElement(), true);
//        Collection<PsiReference> psiReferencesAll = psiReferences.findAll();
//        System.out.println(psiReferencesAll);
        PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);
        @NotNull String[] allClassNames = psiShortNamesCache.getAllClassNames();
        List<PsiClass> psiClasses = new ArrayList<>();
        for (String className : allClassNames) {
            if (className.contains("BeanUtil") || className.contains("TypeUtil")) {
                @NotNull PsiClass[] classesByName = psiShortNamesCache.getClassesByName(className, GlobalSearchScope.allScope(project));
                psiClasses.addAll(Arrays.stream(classesByName).toList());
            }
        }
        System.out.println("PsiClass:");
        for (PsiClass psiClass1 : psiClasses) {
            System.out.println(psiClass1.getQualifiedName());
            Query<PsiReference> classReference = ReferencesSearch.search(psiClass1);
            Collection<PsiReference> classReferenceAll = classReference.findAll();
            for (PsiReference pr : classReferenceAll) {
                if (pr instanceof PsiReferenceExpression pre) {
                    PsiElement _pre = pre;
                    PsiMethodCallExpression pmce = null;
                    for (;;) {
                        if (_pre == null) {
                            break;
                        } if (_pre instanceof PsiMethodCallExpression _pmce) {
                            pmce = _pmce;
                            break;
                        } else {
                            _pre = _pre.getParent();
                        }
                    }
                    if (pmce != null) {
                        PsiExpression[] psiExpressions = pmce.getArgumentList().getExpressions();
                        for (PsiExpression pe : psiExpressions) {
                            if (pe.getType() instanceof PsiClassType pct) {
                                PsiClass searchedClass = pct.resolve();
                                if (searchedClass == psiClass) {
                                    // 查询到了被BeanUtil调用的该类型
                                }
                            }
                        }
                    }
                }
                System.out.println(" -> " + pr.getCanonicalText());
            }
        }
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
