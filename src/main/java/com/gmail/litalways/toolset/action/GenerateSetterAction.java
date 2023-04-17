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
        // 单字符变量优化
        if (psiElement instanceof PsiWhiteSpace && offset != 0) {
            offset--;
            psiElement = file.findElementAt(offset);
        }
        // 非Java标识符则不处理
        if (!(psiElement instanceof PsiIdentifier)) {
            return;
        }
        // 获取叶子的上级元素
        PsiElement psiTargetElementParent = psiElement.getParent();
        PsiType psiTargetType;
        if (psiTargetElementParent instanceof PsiLocalVariable p) {
            psiTargetType = p.getType();
        } else if (psiTargetElementParent instanceof PsiReferenceExpression p) {
            psiTargetType = p.getType();
        } else if (psiTargetElementParent instanceof PsiParameter p) {
            psiTargetType = p.getType();
        } else {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.unexpected.element.type"));
            return;
        }
        // 获取插入点
        PsiElement lastLeaf = getLastLeafFromElement(psiElement);
        if (lastLeaf == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.locate.code.error"));
            return;
        }
        // 获取该变量的引用类型
        PsiClass psiTargetClass;
        if (psiTargetType instanceof PsiClassType) {
            psiTargetClass = ((PsiClassType) psiTargetType).resolve();
        } else {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.cannot.get.class.type"));
            return;
        }
        if (psiTargetClass == null) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.cannot.get.class.type"));
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
        //noinspection DialogTitleCapitalization
        chooser.setTitle(MessageUtil.getMessage("action.generate.setter.choose.methods.copied.by"));
        chooser.setCopyJavadocVisible(true);
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
        if (sourceVarName == null || sourceVarName.trim().length() == 0) {
            return;
        }
        // 获取需要生成Setter的变量名
        String targetElementName = psiElement.getText().trim();
        // 获取缩进
        String indent = "";
        PsiElement firstLeaf = getFirstLeafFromElement(psiElement);
        if (firstLeaf != null) {
            for (;;) {
                if (firstLeaf == null || firstLeaf.getParent() == null) {
                    // 获取失败
                    indent = "";
                    break;
                } if (firstLeaf.getParent().getPrevSibling() instanceof PsiWhiteSpace p && p.getText().startsWith("\n") && p.getText().length() > 1) {
                    // 获取到最左侧叶子带换行的空格Psi，获取缩进格数
                    indent = p.getText().substring(1);
                    break;
                } else {
                    // 该层非最外层，继续向上查询
                    firstLeaf = getFirstLeafFromElement(firstLeaf.getParent());
                    continue;
                }
            }
        }
        // 生成代码
        StringBuilder builder = new StringBuilder("\n");
        for (PsiMethodMember psiMethodMember : selectedElements) {
            PsiMethod psiMethod = psiMethodMember.getElement();
            if (chooser.isCopyJavadoc() && psiMethod.getDocComment() != null) {
                String comment = psiMethod.getDocComment().toString()
                        .replaceAll("/", "")
                        .replaceAll("\\*", "")
                        .replaceAll("\\n", "")
                        .trim();
                builder.append(indent).append("// ").append(comment).append("\n");
            }
            String setMethodName = psiMethod.getName();
            String getMethodName = "g" + setMethodName.substring(1);
            builder.append(indent).append(targetElementName).append(".").append(setMethodName).append("(")
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
     * 选择事件驱动or后台线程
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
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
        PsiElement ele = psiFile.findElementAt(offset);
        if (!(ele instanceof PsiIdentifierImpl)) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    /**
     * 获取该语法树第一个节点
     */
    private static @Nullable PsiElement getFirstLeafFromElement(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiLocalVariable) {
            return parent.getFirstChild();
        } else if (parent instanceof PsiParameter) {
            if (parent.getParent() instanceof PsiParameterList && parent.getParent().getParent() instanceof PsiMethod method) {
                return method.getBody();
            }
            return null;
        } else if (parent instanceof PsiReferenceExpression) {
            if (parent.getParent().getParent().getParent() instanceof PsiExpressionStatement) {
                return parent.getParent().getParent().getParent().getFirstChild();
            }
            return null;
        } else if (parent instanceof PsiStatement) {
            return parent;
        }
        return getFirstLeafFromElement(parent.getParent());
    }

    /**
     * 获取该语法树最后一个节点
     */
    private static @Nullable PsiElement getLastLeafFromElement(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiLocalVariable) {
            return parent.getLastChild();
        } else if (parent instanceof PsiParameter) {
            if (parent.getParent() instanceof PsiParameterList && parent.getParent().getParent() instanceof PsiMethod method) {
                return method.getBody();
            }
            return null;
        } else if (parent instanceof PsiReferenceExpression) {
            if (parent.getParent().getParent().getParent() instanceof PsiExpressionStatement) {
                return parent.getParent().getParent().getParent().getLastChild();
            }
            return null;
        } else if (parent instanceof PsiStatement) {
            return parent;
        }
        return getLastLeafFromElement(parent.getParent());
    }

}
