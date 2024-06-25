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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        // 针对Map类型做特殊判断，不处理数据来源和去向均为Map的情况
        boolean targetIsMap = Map.class.getName().equals(psiTargetClass.getQualifiedName());
        // 提示用户输入Getter来源
        String sourceVarName = Messages.showInputDialog(
                MessageUtil.getMessage("action.generate.setter.input.variable.name.of.source.object"),
                KeyConstant.NOTIFICATION_GROUP_KEY, Messages.getQuestionIcon());
        if (sourceVarName == null || sourceVarName.trim().isEmpty()) {
            return;
        }
        // 获取Getter源的变量引用类型
        boolean sourceIsMap = true;
        PsiClass psiSourceClass = null;
        PsiElement sourceElement = getPsiElementByVariableName(sourceVarName, psiElement);
        if (sourceElement != null) {
            // 能获取到数据来源对象，就判断是不是Map
            PsiType sourceTargetType = getPsiType(sourceElement);
            if (sourceTargetType instanceof PsiClassType sourcePct) {
                psiSourceClass = sourcePct.resolve();
                if (psiSourceClass != null) {
                    String sourceClassName = psiSourceClass.getQualifiedName();
                    if (Map.class.getName().equals(sourceClassName)) {
                        if (targetIsMap) {
                            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.not.support.map"));
                            return;
                        }
                    } else {
                        sourceIsMap = false;
                    }
                }
            }
        }
        // 来源元素若获取不到，默认为vo，但不允许目标元素是map类型,因为要获取字段
        if (targetIsMap && sourceIsMap) {
            NotificationUtil.warning(MessageUtil.getMessage("action.generate.setter.not.support.map"));
            return;
        }
        // 准备提示用户选择哪些字段需要生成Setter
        PsiMethod[] psiTargetClassAllMethods = (targetIsMap ? psiSourceClass : psiTargetClass).getAllMethods();
        List<PsiMethodMember> psiTargetMethodsList = new ArrayList<>();
        for (PsiMethod psiMethod : psiTargetClassAllMethods) {
            if (targetIsMap) {
                if (!psiMethod.getName().startsWith("get")) {
                    continue;
                }
            } else {
                if (!psiMethod.getName().startsWith("set")) {
                    continue;
                }
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
            if (targetIsMap) {
                // 目标是Map，赋值表达式
                String getMethodName = psiMethod.getName();
                String fieldName = getMethodName.substring(3);
                fieldName = fieldName.length() > 1 ? fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1) : fieldName.toLowerCase();
                builder.append(indentWhitespace).append(targetElementName).append(".put(\"").append(fieldName).append("\", ")
                        .append(sourceVarName).append(".").append(getMethodName).append("());\n");
            } else if (sourceIsMap && psiSourceClass != null && Map.class.getName().equals(psiSourceClass.getQualifiedName())) {
                // 来源确定是Map
                String setMethodName = psiMethod.getName();
                String fieldName = setMethodName.substring(3);
                fieldName = fieldName.length() > 1 ? fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1) : fieldName.toLowerCase();
                String useValueOfOrParse = null;
                String fieldCast = "";
                PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
                if (parameters.length > 0) {
                    if (parameters[0].getType() instanceof PsiPrimitiveType methodPt) {
                        // 基础类型直接强转
                        fieldCast = "(" + methodPt.getName() + ") ";
                    } else if (parameters[0].getType() instanceof PsiClassType methodPct) {
                        // 除Character外的包装类型和String做解析，其他强转
                        PsiClass methodClass = methodPct.resolve();
                        if (methodClass != null) {
                            String qualifiedName = methodClass.getQualifiedName();
                            if (String.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : String.valueOf(${get})";
                            } else if (Boolean.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Boolean.parseBoolean(String.valueOf(${get}))";
                            } else if (Short.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Short.parseShort(String.valueOf(${get}))";
                            } else if (Integer.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Integer.parseInt(String.valueOf(${get}))";
                            } else if (Long.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Long.parseLong(String.valueOf(${get}))";
                            } else if (Float.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Float.parseFloat(String.valueOf(${get}))";
                            } else if (Double.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Double.parseDouble(String.valueOf(${get}))";
                            } else if (Byte.class.getName().equals(qualifiedName)) {
                                useValueOfOrParse = "${get} == null ? null : Byte.parseByte(String.valueOf(${get}))";
                            } else {
                                fieldCast = "(" + methodPct.getPresentableText(false) + ") ";
                            }
                        }
                    } else {
                        // 其他类型，如数组
                        fieldCast = "(" + parameters[0].getType().getPresentableText(false) + ") ";
                    }
                }
                builder.append(indentWhitespace).append(targetElementName).append(".").append(setMethodName).append("(");
                if (useValueOfOrParse != null) {
                    builder.append(useValueOfOrParse.replace("${get}", sourceVarName + ".get(\"" + fieldName + "\")")).append(");\n");
                } else {
                    builder.append(fieldCast).append(sourceVarName).append(".get(\"").append(fieldName).append("\"));\n");
                }
            } else {
                // 来源不确定或没有Map参与本次执行
                String setMethodName = psiMethod.getName();
                String getMethodName = "g" + setMethodName.substring(1);
                builder.append(indentWhitespace).append(targetElementName).append(".").append(setMethodName).append("(")
                        .append(sourceVarName).append(".").append(getMethodName).append("());\n");
            }
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

    /**
     * 向上寻找指定实例名的Psi对象
     *
     * @param variableName      实例名
     * @param beforeThisElement 开始寻找的Psi节点
     * @return 寻找到的Psi对象
     */
    private static @Nullable PsiElement getPsiElementByVariableName(@NotNull String variableName, @Nullable PsiElement beforeThisElement) {
        if (beforeThisElement == null) {
            return null;
        }
        PsiElement nowElement = beforeThisElement;
        // 需要向上找
        boolean needUp = true;
        do {
            if (needUp) {
                PsiElement prevSibling = nowElement.getPrevSibling();
                if (prevSibling != null) {
                    nowElement = prevSibling;
                } else {
                    PsiElement parent = nowElement.getParent();
                    while (true) {
                        if (parent == null) {
                            break;
                        }
                        PsiElement parentPrev = parent.getPrevSibling();
                        if (parentPrev == null) {
                            parent = parent.getParent();
                        } else {
                            parent = parentPrev;
                            break;
                        }
                    }
                    if (parent != null) {
                        nowElement = parent;
                    } else {
                        break;
                    }
                }
                needUp = false;
            }
            if (nowElement instanceof PsiBlockStatement) {
                // 特例：如果是代码块，则跳过
                needUp = true;
            } else if (checkPsiElementName(variableName, nowElement)) {
                // 检查是符合条件的对象，直接返回
                return nowElement;
            } else {
                // 其他情况
                PsiElement[] children = nowElement.getChildren();
                if (children.length > 0) {
                    // 从children的末尾开始循环判断
                    nowElement = children[children.length - 1];
                } else {
                    needUp = true;
                }
            }
        } while (!(nowElement instanceof PsiFile));
        return nowElement;
    }

    /**
     * 根据参数名判断Psi对象
     *
     * @param variableName 参数名
     * @param node         当前需要判断的Psi对象
     * @return 是否等于
     */
    private static boolean checkPsiElementName(@NotNull String variableName, PsiElement node) {
        if (node instanceof PsiNamedElement v) {
            String name = v.getName();
            return variableName.equals(name);
        }
        return false;
    }

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
            // 表达式，取最外层表达式的最后元素（目前对于if等表达式内写法会造成意外情况）
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
        if (psiElement instanceof PsiLocalVariable p) {
            // 直接赋值 var a = b; / try (Object a = b();) {}
            return p.getType();
        } else if (psiElement instanceof PsiReferenceExpression && psiElement.getFirstChild() instanceof PsiReferenceExpression p) {
            // 引用表达式取头部引用对象的Type
            return p.getType();
        } else if (psiElement instanceof PsiReferenceExpression p && psiElement.getFirstChild() instanceof PsiReferenceParameterList) {
            // if (a == b) {} / Object a; a = b; / NOT test(a, b);
            return p.getType();
        } else if (psiElement instanceof PsiExpression p) {
            // ?
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
