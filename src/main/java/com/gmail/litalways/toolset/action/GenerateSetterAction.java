package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 生成Setter方法
 *
 * @author IceRain
 * @since 2023/04/17
 */
public class GenerateSetterAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        PsiFile file = anActionEvent.getData(LangDataKeys.PSI_FILE);
        if (project == null || editor == null || file == null) {
            return;
        }
        // 获取光标坐标（光标右侧字符）
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = file.findElementAt(offset);
        // 光标在标识符末尾的优化
        if (psiElement != null && !(psiElement instanceof PsiIdentifier) && psiElement.getPrevSibling() instanceof PsiIdentifier && offset != 0) {
            offset--;
            psiElement = file.findElementAt(offset);
        }
        // 非Java标识符则不处理
        if (!(psiElement instanceof PsiIdentifier)) {
            return;
        }
        // 获取表达式操作的实例类型
        PsiType psiTargetType = getPsiType(psiElement.getParent());
        if (psiTargetType == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.unexpected.element.type"));
            return;
        }
        // 获取表达式末尾的插入点
        PsiElement lastLeaf = getInsertPointOfStatement(psiElement);
        if (lastLeaf == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.locate.code.error"));
            return;
        }
        // 获取该变量的引用类型
        PsiClass psiTargetClass;
        if (psiTargetType instanceof PsiClassType pct) {
            psiTargetClass = pct.resolve();
        } else {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.cannot.get.class.type"));
            return;
        }
        if (psiTargetClass == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.cannot.get.class.type"));
            return;
        }
        // 不处理Map类型 TODO 读取值来源的psi，仅不处理都是map的情况
        if (Map.class.getName().equals(psiTargetClass.getQualifiedName())) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.not.support.map"));
            return;
        }
        PsiMethod[] psiTargetClassAllMethods = psiTargetClass.getAllMethods();
        List<PsiMethodMember> psiTargetMethodsList = new ArrayList<>();
        for (PsiMethod psiMethod : psiTargetClassAllMethods) {
            if (!psiMethod.getName().startsWith("set")) {
                continue;
            }
            psiTargetMethodsList.add(new PsiMethodMember(psiMethod));
        }
        PsiMethodMember[] psiTargetMethods = psiTargetMethodsList.toArray(new PsiMethodMember[0]);
        if (psiTargetMethods.length < 1) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.no.methods.in.target.class"));
            return;
        }
        // 提示用户选择哪些字段需要生成Setter
        MemberChooser<PsiMethodMember> chooser = new MemberChooser<>(psiTargetMethods, false, true, project);
        chooser.setTitle(MessageUtil.getMessage("action.generate.setter.choose.methods.copied.by"));
//        chooser.setCopyJavadocVisible(true);
//        FileDocumentManager.getInstance().saveAllDocuments();
        chooser.show();
        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return;
        }
        List<PsiMethodMember> selectedElements = chooser.getSelectedElements();
        if (selectedElements == null || selectedElements.isEmpty()) {
            return;
        }
        // 提示用户输入Getter来源
        String sourceVarName = Messages.showInputDialog(
                MessageUtil.getMessage("action.generate.setter.input.variable.name.of.source.object"),
                KeyConstant.NOTIFICATION_GROUP_KEY, Messages.getQuestionIcon());
        if (sourceVarName == null || sourceVarName.trim().isEmpty()) {
            return;
        }
        // TODO 获取Getter源的变量引用类型

        // 获取需要生成Setter的变量名
        String targetElementName = psiElement.getText().trim();
        // 获取缩进
        String indentWhitespace = getIndentWhitespace(psiElement);
        if (indentWhitespace == null) {
            indentWhitespace = "";
        }
        // 生成代码
        StringBuilder builder = new StringBuilder("\n");
        for (PsiMethodMember psiMethodMember : selectedElements) {
            PsiMethod psiMethod = psiMethodMember.getElement();
//            if (chooser.isCopyJavadoc() && psiMethod.getDocComment() != null) {
//                String comment = psiMethod.getDocComment().toString()
//                        .replaceAll("/", "")
//                        .replaceAll("\\*", "")
//                        .replaceAll("\\n", "")
//                        .trim();
//                builder.append(indentWhitespace).append("// ").append(comment).append("\n");
//            }
            String setMethodName = psiMethod.getName();
            String getMethodName = "g" + setMethodName.substring(1);
            builder.append(indentWhitespace).append(targetElementName).append(".").append(setMethodName).append("(")
                    .append(sourceVarName).append(".").append(getMethodName).append("());\n");
        }
        String builderString = builder.toString();
        if (builderString.endsWith("\n")) {
            builderString = builderString.substring(0, builderString.length() - 1);
        }
        final String builderStr = builderString;
        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.get.document.failed"));
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.insertString(lastLeaf.getTextOffset() + 1, builderStr);
        });
    }

    /**
     * 选择Event-Dispatching-Thread或Background-Thread，事件驱动线程的update不能访问psi等文件系统，后台线程的update不能直接访问UI控件，使用`AnActionEvent.getUpdateSession().compute()`在BGT中进行EDT的操作
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        PsiClass currentClass = PsiTreeUtil.findChildOfAnyType(psiFile.getOriginalElement(), PsiClass.class);
        if (currentClass == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        // 选择在标识符末尾的优化
        if (psiElement != null && !(psiElement instanceof PsiIdentifier) && psiElement.getPrevSibling() instanceof PsiIdentifier && offset != 0) {
            offset--;
            psiElement = psiFile.findElementAt(offset);
        }
        // 屏蔽TOKEN
        if (!(psiElement instanceof PsiIdentifierImpl)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
    }

//    private static PsiElement getPsiElementByVariableName(String variableName, @Nullable PsiElement beforeThisElement) {
//        if (beforeThisElement == null) {
//            return null;
//        }
//    }

    /**
     * 查找该语句的换行标识符
     *
     * @param psiElement PsiIdentifier
     * @return IndentSpace
     */
    private static @Nullable String getIndentWhitespace(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiLocalVariable) {
            // 声明实例对象
            return getIndentWhitespace(parent);
        } else if (parent instanceof PsiParameter) {
            // 方法参数
            if (parent.getParent() instanceof PsiParameterList ppl
                    && ppl.getParent() instanceof PsiMethod pm
                    && pm.getBody() != null && pm.getBody().getFirstChild().getNextSibling() instanceof PsiWhiteSpace p
                    && p.getText().startsWith("\n") && p.getText().length() > 1) {
                return p.getText().substring(1) + "    ";
            }
        } else if (parent instanceof PsiMethod pm
                && pm.getPrevSibling() instanceof PsiWhiteSpace p
                && p.getText().startsWith("\n") && p.getText().length() > 1) {
            // 方法签名
            return p.getText().substring(1);
        } else if (parent instanceof PsiExpression) {
            // 引用对象
            return getIndentWhitespace(parent);
        } else if (parent instanceof PsiStatement ps
                && ps.getPrevSibling() instanceof PsiWhiteSpace p
                && p.getText().startsWith("\n") && p.getText().length() > 1) {
            // 块
            return p.getText().substring(1);
        } else if (parent instanceof PsiCodeBlock pcb
                && pcb.getPrevSibling() instanceof PsiWhiteSpace p
                && p.getText().startsWith("\n") && p.getText().length() > 1) {
            // 代码块
            return p.getText().substring(1);
        } else if (parent instanceof PsiWhiteSpace p
                && p.getText().startsWith("\n") && p.getText().length() > 1) {
            return p.getText().substring(1);
        }
        return getIndentWhitespace(parent);
    }

    /**
     * 获取插入点
     *
     * @param psiElement PsiIdentifier
     * @return PsiElement
     */
    private static @Nullable PsiElement getInsertPointOfStatement(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiLocalVariable) {
            // 声明实例对象，获取行结尾分号
            return parent.getLastChild();
        } else if (parent instanceof PsiParameter) {
            // 方法参数，获取代码块
            if (parent.getParent() instanceof PsiParameterList ppl && ppl.getParent() instanceof PsiMethod pm) {
                return pm.getBody();
            }
        } else if (parent instanceof PsiMethod pm) {
            return pm.getBody();
        } else if (parent instanceof PsiExpression pe) {
            // 表达式，取最外层表达式的最后元素
            if (pe.getParent() instanceof PsiExpression) {
                return getInsertPointOfStatement(parent);
            } else {
                return pe.getNextSibling();
            }
        } else if (parent instanceof PsiStatement) {
            return parent.getLastChild();
        } else if (parent instanceof PsiCodeBlock) {
            return parent;
        }
        return getInsertPointOfStatement(parent.getParent());
    }

    /**
     * 获取类型
     *
     * @param psiElement PsiIdentifier
     * @return PsiType
     */
    private static @Nullable PsiType getPsiType(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
//        Object a;
//        Object b;
//        if (a == b) {} // TODO 拿不到？
//        if (a.a() == b) {} // TODO 拿不到？
//        a = b; // TODO 拿不到a？且拿b会因getFirstChild拿到a
        if (psiElement instanceof PsiLocalVariable p) {
            // 直接赋值 var a = b; / try (Object a = b();) {}
            return p.getType();
        } else if (psiElement instanceof PsiReferenceExpression && psiElement.getFirstChild() instanceof PsiReferenceExpression p) {
            // 引用表达式取头部引用对象的Type if (a == b) {} / Object a; a = b; / NOT test(a, b);
            return p.getType();
        } else if (psiElement instanceof PsiJavaCodeReferenceElement pre) {
            if (pre.getParent() instanceof PsiTypeElement pte) {
                // 赋值表达式左侧类型对象
                return pte.getType();
            } else if (pre.getParent() instanceof PsiNewExpression pne) {
                // 赋值表达式左侧变量+右侧
                if (pne.getParent() instanceof PsiLocalVariable p) {
                    return p.getType();
                } else if (pne.getParent() instanceof PsiAssignmentExpression pae) {
                    return pae.getType();
                } else {
                    // 非赋值表达式则不获取类型，否则直接对PsiNewExpression获取类型即可
                    return null;
                }
            }
        } else if (psiElement instanceof PsiParameter p) {
            // 方法参数 public void someMethod(Object targetObj) {} / if (a instanceof Object targetB) {}
            return p.getType();
        }
        return null;
    }

}
