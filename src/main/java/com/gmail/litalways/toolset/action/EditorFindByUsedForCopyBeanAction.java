package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.state.MainSettingState;
import com.gmail.litalways.toolset.state.MainSettingsClassName;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
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
            doFindWithInstance(project, editor, plv);
        } else if (psiElement.getParent() instanceof PsiReferenceExpression pre) {
            // 表达式内参数实例
            doFindWithInstance(project, editor, pre);
        } else if (psiElement.getParent() instanceof PsiParameter pp) {
            // 方法参数实例
            doFindWithInstance(project, editor, pp);
        } else if (psiElement.getParent() instanceof PsiJavaCodeReferenceElement pjcre) {
            // 类
            if (pjcre.getParent() instanceof PsiTypeElement pte) {
                PsiType psiType = pte.getType();
                doFindWithType(project, editor, psiType);
            } else if (pjcre.getParent() instanceof PsiNewExpression pne) {
                PsiType psiType = pne.getType();
                doFindWithType(project, editor, psiType);
            }
        }
    }

    void doFindWithInstance(Project project, Editor editor, PsiElement psiElement) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.usage.by.bean.utils.searching")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // BGT
                Query<PsiReference> psiReferences = ReferencesSearch.search(psiElement);
                Collection<PsiReference> psiReferencesAll = psiReferences.findAll();
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT with read access
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
                                    PsiType psiType = qualifierExpression.getType(); // null if expression is static call
                                    PsiElement qualifier = pmce.getMethodExpression().getQualifier(); // if psiType is not null, this is instance call, if not, use getText() and find class to get qualifier name
                                    String methodName = psiMethod == null ? null : psiMethod.getText();
                                    if (psiType == null) {
                                        // static call
                                        String simpleClassName = qualifier != null ? qualifier.getText() : qualifierExpression.getText();
                                        @NotNull PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(simpleClassName, GlobalSearchScope.allScope(project));
                                        for (PsiClass pc : classesByName) {
                                            boolean find = false;
                                            String qualifierClassname = pc.getQualifiedName();
                                            for (MainSettingsClassName conf : MainSettingState.getInstance().beanUtilsClassName) {
                                                if (conf.getSimpleClassName() != null && conf.getSimpleClassName().trim().length() > 0 && !conf.getSimpleClassName().equals(simpleClassName)) {
                                                    continue;
                                                }
                                                if (conf.getQualifierClassName() != null && conf.getQualifierClassName().trim().length() > 0 && !conf.getQualifierClassName().equals(qualifierClassname)) {
                                                    continue;
                                                }
                                                if (conf.getMethodName() != null && conf.getMethodName().trim().length() > 0 && !conf.getMethodName().equals(methodName)) {
                                                    continue;
                                                }
                                                // 查询到了被BeanUtil调用的该类型
                                                filteredReference.add(ref);
                                                find = true;
                                                break;
                                            }
                                            if (find) {
                                                break;
                                            }
                                        }
                                    } else {
                                        // instance call
                                        String simpleClassName;
                                        String qualifierClassname;
                                        PsiClass psiClass = ((PsiClassType) psiType).resolve();
                                        if (psiClass == null) {
                                            simpleClassName = null;
                                            qualifierClassname = null;
                                        } else {
                                            simpleClassName = psiClass.getName();
                                            qualifierClassname = psiClass.getQualifiedName();
                                        }
                                        for (MainSettingsClassName conf : MainSettingState.getInstance().beanUtilsClassName) {
                                            if (conf.getSimpleClassName() != null && conf.getSimpleClassName().trim().length() > 0 && !conf.getSimpleClassName().equals(simpleClassName)) {
                                                continue;
                                            }
                                            if (conf.getQualifierClassName() != null && conf.getQualifierClassName().trim().length() > 0 && !conf.getQualifierClassName().equals(qualifierClassname)) {
                                                continue;
                                            }
                                            if (conf.getMethodName() != null && conf.getMethodName().trim().length() > 0 && !conf.getMethodName().equals(methodName)) {
                                                continue;
                                            }
                                            // 查询到了被BeanUtil调用的该类型
                                            filteredReference.add(ref);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 收集结果集并显示
                    if (filteredReference.size() == 0) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            HintManager.getInstance().showInformationHint(editor, MessageUtil.getMessage("action.find.usage.by.bean.utils.not.found"));
                        });
                    } else {
                        List<Usage> usages = new ArrayList<>();
                        for (PsiReference reference : filteredReference) {
                            UsageInfo usageInfo = new UsageInfo(reference);
                            Usage usage = new UsageInfo2UsageAdapter(usageInfo);
                            usages.add(usage);
                        }
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            UsageViewPresentation presentation = new UsageViewPresentation();
                            presentation.setTabText(MessageUtil.getMessage("action.find.usage.by.bean.utils.title"));
                            UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[0]), presentation);
                        });
                    }
                });
            }
        });
    }

    void doFindWithType(Project project, Editor editor, PsiType psiType) {
        PsiClass psiClass = ((PsiClassType) psiType).resolve();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.usage.by.bean.utils.searching")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // BGT
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT with read access
                    // 获取全部BeanUtil类
                    PsiShortNamesCache allClassCache = PsiShortNamesCache.getInstance(project);
                    Set<PsiClass> beanUtilClasses = new HashSet<>();
                    for (MainSettingsClassName conf : MainSettingState.getInstance().beanUtilsClassName) {
                        if (conf.getQualifierClassName() != null && conf.getQualifierClassName().trim().length() > 0) {
                            @NotNull PsiClass[] classesByName = allClassCache.getClassesByName(conf.getQualifierClassName(), GlobalSearchScope.allScope(project));
                            Collections.addAll(beanUtilClasses, classesByName);
                        } else if (conf.getSimpleClassName() != null && conf.getSimpleClassName().trim().length() > 0) {
                            @NotNull PsiClass[] classesByName = allClassCache.getClassesByName(conf.getSimpleClassName(), GlobalSearchScope.allScope(project));
                            Collections.addAll(beanUtilClasses, classesByName);
                        }
                    }
                    Map<PsiClass, Collection<PsiReference>> beanUtilClassesRef = new HashMap<>();
                    for (PsiClass beanUtilClass : beanUtilClasses) {
                        Query<PsiReference> classReference = ReferencesSearch.search(beanUtilClass);
                        Collection<PsiReference> classReferenceAll = classReference.findAll();
                        beanUtilClassesRef.put(beanUtilClass, classReferenceAll);
                    }
                    // 筛选出使用这些BeanUtil调用目标类的结果集
                    Collection<PsiReference> filteredReference = new ArrayList<>();
                    for (Map.Entry<PsiClass, Collection<PsiReference>> entry : beanUtilClassesRef.entrySet()) {
                        PsiClass beanUtilClass = entry.getKey();
                        String qualifierClassname = beanUtilClass.getQualifiedName();
                        String simpleClassName = beanUtilClass.getName();
                        Collection<PsiReference> classReferenceAll = entry.getValue();
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
                                    // 方法名匹配
                                    @NotNull PsiElement[] expChildren = pmce.getMethodExpression().getChildren();
                                    PsiIdentifier psiMethodExp = null;
                                    for (int i = expChildren.length - 1; i >= 0; i--) {
                                        if (expChildren[i] instanceof PsiIdentifier pi) {
                                            psiMethodExp = pi;
                                            break;
                                        }
                                    }
                                    String methodName = psiMethodExp == null ? null : psiMethodExp.getText();
                                    boolean methodMatch = true;
                                    if (methodName != null) {
                                        methodMatch = false;
                                        for (MainSettingsClassName conf : MainSettingState.getInstance().beanUtilsClassName) {
                                            if (conf.getSimpleClassName() != null && conf.getSimpleClassName().trim().length() > 0 && !conf.getSimpleClassName().equals(simpleClassName)) {
                                                continue;
                                            }
                                            if (conf.getQualifierClassName() != null && conf.getQualifierClassName().trim().length() > 0 && !conf.getQualifierClassName().equals(qualifierClassname)) {
                                                continue;
                                            }
                                            if (conf.getMethodName() != null && conf.getMethodName().trim().length() > 0 && !conf.getMethodName().equals(methodName)) {
                                                continue;
                                            }
                                            methodMatch = true;
                                            break;
                                        }
                                    }
                                    if (!methodMatch) {
                                        continue;
                                    }
                                    // 该方法参数是否是当前选中的类型
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
                    if (filteredReference.size() == 0) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            HintManager.getInstance().showInformationHint(editor, MessageUtil.getMessage("action.find.usage.by.bean.utils.not.found"));
                        });
                    } else {
                        List<Usage> usages = new ArrayList<>();
                        for (PsiReference reference : filteredReference) {
                            UsageInfo usageInfo = new UsageInfo(reference);
                            Usage usage = new UsageInfo2UsageAdapter(usageInfo);
                            usages.add(usage);
                        }
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            UsageViewPresentation presentation = new UsageViewPresentation();
                            presentation.setTabText(MessageUtil.getMessage("action.find.usage.by.bean.utils.title"));
                            UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[0]), presentation);
                        });
                    }
                });
            }
        });
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
