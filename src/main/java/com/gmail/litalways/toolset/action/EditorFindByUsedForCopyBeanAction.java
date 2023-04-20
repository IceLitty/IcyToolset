package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 在Editor控件显示编辑提示，用以寻找该类/实例是否被各类BeanUtil进行拷贝
 * TODO 对两处搜索功能做UI配置项，提供类名、完整包名甚至是方法名的配置，进行针对性搜索
 *
 * @author IceRain
 * @since 2023/04/17
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class EditorFindByUsedForCopyBeanAction implements IntentionAction {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("message");

    /**
     * 操作提示
     */
    @Override
    public @IntentionName @NotNull String getText() {
        return messageBundle.getString("action.com.gmail.litalways.toolset.action.find.by.bean.copy.text");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return messageBundle.getString("action.com.gmail.litalways.toolset.action.find.by.bean.copy.text");
    }

    /**
     * 是否显示提示
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (editor == null || psiFile == null) {
            return false;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        // 选择在标识符末尾的优化
        if (psiElement != null && !(psiElement instanceof PsiIdentifier) && psiElement.getPrevSibling() instanceof PsiIdentifier && offset != 0) {
            offset--;
            psiElement = psiFile.findElementAt(offset);
        }
        // 非Java标识符则不处理
        return psiElement instanceof PsiIdentifier;
    }

    /**
     * 预览操作或执行操作
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        // 选择在标识符末尾的优化
        if (psiElement != null && !(psiElement instanceof PsiIdentifier) && psiElement.getPrevSibling() instanceof PsiIdentifier && offset != 0) {
            offset--;
            psiElement = psiFile.findElementAt(offset);
        }
        // 非Java标识符则不处理
        if (!(psiElement instanceof PsiIdentifier)) {
            return;
        }
        if (psiElement.getParent() instanceof PsiLocalVariable plv) {
            // 实例
            doFindWithInstance(project, plv);
        } else if (psiElement.getParent() instanceof PsiReferenceExpression pre) {
            // 表达式内参数实例
            doFindWithInstance(project, pre);
        } else if (psiElement.getParent() instanceof PsiParameter pp) {
            // 方法参数实例
            doFindWithInstance(project, pp);
        } else if (psiElement.getParent() instanceof PsiJavaCodeReferenceElement pjcre) {
            // 类
            if (pjcre.getParent() instanceof PsiTypeElement pte) {
                PsiType psiType = pte.getType();
                doFindWithType(project, psiType);
            } else if (pjcre.getParent() instanceof PsiNewExpression pne) {
                PsiType psiType = pne.getType();
                doFindWithType(project, psiType);
            }
        }
    }

    void doFindWithInstance(Project project, PsiElement psiElement) {
        Query<PsiReference> psiReferences = ReferencesSearch.search(psiElement);
        Collection<PsiReference> psiReferencesAll = psiReferences.findAll();
        Collection<PsiReference> filteredReference = new ArrayList<>();
        for (PsiReference ref : psiReferencesAll) {
            if (ref instanceof PsiReferenceExpression) {
                PsiElement pre = (PsiReferenceExpression) ref;
                PsiMethodCallExpression pmce = null;
                while (true) {
                    // 向上查找到最外层/方法体则中止，表明该表达式不是方法调用
                    if (pre == null || pre instanceof PsiMethod) {
                        break;
                    }
                    if (pre instanceof PsiMethodCallExpression) {
                        pmce = (PsiMethodCallExpression) pre;
                        break;
                    } else {
                        pre = pre.getParent();
                    }
                }
                if (pmce != null) {
                    PsiExpression qualifierExpression = pmce.getMethodExpression().getQualifierExpression(); // getText() to class name
                    if (qualifierExpression != null) {
                        PsiIdentifier psiMethod = null;
                        if (qualifierExpression.getNextSibling() instanceof PsiJavaToken pt
                                && pt.getNextSibling() instanceof PsiReferenceParameterList ppl
                                && ppl.getNextSibling() instanceof PsiIdentifier p) {
                            psiMethod = p;
                        }
                        String className = qualifierExpression.getText();
                        if (className.contains("BeanUtil") || className.contains("TypeUtil")) {
                            // 查询到了被BeanUtil调用的该类型
                            filteredReference.add(ref);
                        }
                    }
                }
            }
        }
        // 收集结果集并显示
        List<Usage> usages = new ArrayList<>();
        for (PsiReference reference : filteredReference) {
            UsageInfo usageInfo = new UsageInfo(reference);
            Usage usage = new UsageInfo2UsageAdapter(usageInfo);
            usages.add(usage);
        }
        UsageViewPresentation presentation = new UsageViewPresentation();
        presentation.setTabText(MessageUtil.getMessage("action.find.usage.by.bean.utils.title"));
        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[0]), presentation);
    }

    void doFindWithType(Project project, PsiType psiType) {
        PsiClass psiClass = ((PsiClassType) psiType).resolve();
        // 获取全部疑似BeanUtil的类
        PsiShortNamesCache allClassCache = PsiShortNamesCache.getInstance(project);
        @NotNull String[] allClassNames = allClassCache.getAllClassNames();
        List<PsiClass> beanUtilClasses = new ArrayList<>();
        for (String className : allClassNames) {
            if (className.contains("BeanUtil") || className.contains("TypeUtil")) {
                @NotNull PsiClass[] classesByName = allClassCache.getClassesByName(className, GlobalSearchScope.allScope(project));
                beanUtilClasses.addAll(Arrays.stream(classesByName).toList());
            }
        }
        // 筛选出使用这些BeanUtil调用目标类的结果集
        Collection<PsiReference> filteredReference = new ArrayList<>();
        for (PsiClass beanUtilClass : beanUtilClasses) {
            Query<PsiReference> classReference = ReferencesSearch.search(beanUtilClass);
            Collection<PsiReference> classReferenceAll = classReference.findAll();
            for (PsiReference ref : classReferenceAll) {
                if (ref instanceof PsiReferenceExpression) {
                    PsiElement pre = (PsiReferenceExpression) ref;
                    PsiMethodCallExpression pmce = null;
                    while (true) {
                        // 向上查找到最外层/方法体则中止，表明该表达式不是方法调用
                        if (pre == null || pre instanceof PsiMethod) {
                            break;
                        }
                        if (pre instanceof PsiMethodCallExpression) {
                            pmce = (PsiMethodCallExpression) pre;
                            break;
                        } else {
                            pre = pre.getParent();
                        }
                    }
                    if (pmce != null) {
                        PsiExpression[] psiExpressions = pmce.getArgumentList().getExpressions();
                        for (PsiExpression pe : psiExpressions) {
                            if (pe.getType() instanceof PsiClassType pct) {
                                PsiClass searchedClass = pct.resolve();
                                if (searchedClass == psiClass) {
                                    // 查询到了被BeanUtil调用的该类型
                                    filteredReference.add(ref);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 收集结果集并显示
        List<Usage> usages = new ArrayList<>();
        for (PsiReference reference : filteredReference) {
            UsageInfo usageInfo = new UsageInfo(reference);
            Usage usage = new UsageInfo2UsageAdapter(usageInfo);
            usages.add(usage);
        }
        UsageViewPresentation presentation = new UsageViewPresentation();
        presentation.setTabText(MessageUtil.getMessage("action.find.usage.by.bean.utils.title"));
        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[0]), presentation);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

//    /**
//     * 代码生成预览
//     */
//    @Override
//    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
//        return IntentionAction.super.generatePreview(project, editor, file);
//    }

}
