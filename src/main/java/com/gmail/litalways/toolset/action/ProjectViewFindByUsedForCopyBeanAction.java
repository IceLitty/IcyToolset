package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 寻找该右键出来文件的类是否被各种BeanUtil操作过，并显示在Find Usage窗口
 *
 * @author IceRain
 * @since 2023/04/14
 */
public class ProjectViewFindByUsedForCopyBeanAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        if (project == null || file == null) {
            return;
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.usage.by.bean.utils.searching")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // BGT
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT with read access
                    // 获取选择的类
                    PsiClass psiClass = PsiTreeUtil.findChildOfAnyType(file, PsiClass.class);
                    // 获取全部疑似BeanUtil的类
                    PsiShortNamesCache allClassCache = PsiShortNamesCache.getInstance(project);
                    @NotNull String[] allClassNames = allClassCache.getAllClassNames();
                    Set<PsiClass> beanUtilClasses = new HashSet<>();
                    for (String className : allClassNames) {
                        @NotNull PsiClass[] classesByName = allClassCache.getClassesByName(className, GlobalSearchScope.allScope(project));
                        for (PsiClass psiClass1 : classesByName) {
                            String qualifiedName = psiClass1.getQualifiedName();
                            String simpleClassName = psiClass1.getName();
                            if (simpleClassName != null && (simpleClassName.contains("BeanUtil") || simpleClassName.contains("TypeUtil"))) {
                                beanUtilClasses.add(psiClass1);
                            }
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
                    ApplicationManager.getApplication().invokeLater(() -> {
                        // EDT
                        UsageViewPresentation presentation = new UsageViewPresentation();
                        presentation.setTabText(MessageUtil.getMessage("action.find.usage.by.bean.utils.title"));
                        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[0]), presentation);
                    });
                });
            }
        });
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
