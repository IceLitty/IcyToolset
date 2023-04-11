package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.service.ToolWindowScriptEditorService;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.Language;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.script.LuaScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.python.jsr223.PyScriptEngineFactory;

import javax.script.ScriptEngineManager;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormScriptFunction {

    private final ToolWindowScript component;
    private final AtomicBoolean injectedNashorn = new AtomicBoolean(false);

    public MainFormScriptFunction(ToolWindowScript component) {
        this.component = component;
//        this.component.textareaScriptSource.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                if (component.checkScriptAutoRun.isSelected()) {
//                    eval();
//                }
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                if (component.checkScriptAutoRun.isSelected()) {
//                    eval();
//                }
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//            }
//        });
        this.component.textareaScriptSourceEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (component.checkScriptAutoRun.isSelected()) {
                    eval();
//                    AutoPopupController.getInstance(component.getCurrentProject()).scheduleAutoPopup(component.textareaScriptSourceEditor);
//                    psiRunner();
                }
            }
        });
//        this.component.textareaScriptSource.setFileType(JavaFileType.INSTANCE);
//        this.component.textareaScriptSource.setOneLineMode(false);
//        this.component.textareaScriptSource.setNewDocumentAndFileType(JavaFileType.INSTANCE, EditorFactory.getInstance().createDocument("System.out.println(\"asd\")"));
//        EditorFactory.getInstance().
//        NotificationUtil.info(component.textareaScriptSource.getFileType().getName());
//        Editor editor = this.component.textareaScriptSource.getEditor();
//        if (editor == null) {
//            NotificationUtil.warning("editor is null");
//        } else {
//            NotificationUtil.info("kind:" + editor.getEditorKind());
//            NotificationUtil.info("highlighter:" + editor.getHighlighter());
//        }
        this.component.radioScriptJavascript.addActionListener(this::radioChanged);
        this.component.radioScriptPython.addActionListener(this::radioChanged);
        this.component.radioScriptLua.addActionListener(this::radioChanged);
        this.component.radioScriptGroovy.addActionListener(this::radioChanged);
        ScrollbarSyncListener syncListener = new ScrollbarSyncListener(this.component.scrollScriptSource, this.component.scrollScriptResult);
        this.component.scrollScriptSource.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptSource.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptResult.getVerticalScrollBar().addAdjustmentListener(syncListener);
        this.component.scrollScriptResult.getHorizontalScrollBar().addAdjustmentListener(syncListener);
        injectNashorn();
    }

//    private void psiRunner() {
//        PsiFile psiFile = PsiDocumentManager.getInstance(component.getCurrentProject())
//                .getPsiFile(component.textareaScriptSourceEditor.getDocument());
//        NotificationUtil.info(psiFile != null ? psiFile.getFileType().getName() : "file type is null");
//    }

//    public static @org.jetbrains.annotations.NotNull com.intellij.lexer.Lexer createDefaultLexer() {
//        // https://dploeger.github.io/intellij-api-doc/com/intellij/psi/tree/IElementType.html
//        return new com.intellij.lexer.MergingLexerAdapter(
//                new com.intellij.lang.java.lexer.JavaLexer(com.intellij.pom.java.LanguageLevel.JDK_17),
//                com.intellij.psi.tree.TokenSet.create(
//                        new com.intellij.psi.tree.IElementType("JAVA", com.intellij.lang.Language.findLanguageByID("Java"))
//                )
//        );
//    }

    private void radioChanged(ActionEvent e) {
        JRadioButton radioButton = (JRadioButton) e.getSource();
        if (radioButton.isSelected()) {
//            ToolWindowScriptEditorService toolWindowScriptEditorService = this.component.getCurrentProject().getService(ToolWindowScriptEditorService.class);
//            if (radioButton == this.component.radioScriptJavascript) {
//                if (!"JAVASCRIPT".equals(toolWindowScriptEditorService.getLastFileType())) {
//                    toolWindowScriptEditorService.setLastFileType("JAVASCRIPT");
//                }
//            } else if (radioButton == this.component.radioScriptPython) {
//                if (!"PYTHON".equals(toolWindowScriptEditorService.getLastFileType())) {
//                    toolWindowScriptEditorService.setLastFileType("PYTHON");
//                }
//            } else if (radioButton == this.component.radioScriptLua) {
//                if (!"LUA".equals(toolWindowScriptEditorService.getLastFileType())) {
//                    toolWindowScriptEditorService.setLastFileType("LUA");
//                }
//            } else if (radioButton == this.component.radioScriptGroovy) {
//                if (!"GROOVY".equals(toolWindowScriptEditorService.getLastFileType())) {
//                    toolWindowScriptEditorService.setLastFileType("GROOVY");
//                }
//            }
            if (this.component.checkScriptAutoRun.isSelected()) {
                eval();
            }
        }
    }

    private void injectNashorn() {
        if (injectedNashorn.compareAndSet(false, true)) {
            try {
                Field[] declaredFields = ScriptUtil.class.getDeclaredFields();
                ScriptEngineManager manager = null;
                for (Field f : declaredFields) {
                    if (f.getType() == ScriptEngineManager.class) {
                        f.setAccessible(true);
                        manager = (ScriptEngineManager) f.get(null);
                    }
                }
                if (manager != null) {
                    NashornScriptEngineFactory nashornScriptEngineFactory = new NashornScriptEngineFactory();
                    manager.registerEngineName("js", nashornScriptEngineFactory);
                    LuaScriptEngineFactory luaScriptEngineFactory = new LuaScriptEngineFactory();
                    manager.registerEngineName("lua", luaScriptEngineFactory);
                    PyScriptEngineFactory pyScriptEngineFactory = new PyScriptEngineFactory();
                    manager.registerEngineName("python", pyScriptEngineFactory);
                }
            } catch (Exception e) {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.nashorn.inject.fail"), e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
            }
        }
    }

    private void eval() {
        try {
            String script = this.component.textareaScriptSourceEditor.getDocument().getText();
            if (this.component.radioScriptJavascript.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getJsEngine().eval(script)));
            } else if (this.component.radioScriptLua.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getLuaEngine().eval(script)));
            } else if (this.component.radioScriptGroovy.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getGroovyEngine().eval(script)));
            } else if (this.component.radioScriptPython.isSelected()) {
                this.component.textareaScriptResult.setText(String.valueOf(ScriptUtil.getPythonEngine().eval(script)));
            } else {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.not.select.type"));
            }
        } catch (Exception ex) {
            this.component.textareaScriptResult.setText(ex.getClass().getName() + ": " + ex.getLocalizedMessage());
        }
    }

}
