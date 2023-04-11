package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.service.ToolWindowScriptEditorService;
import com.gmail.litalways.toolset.state.ScriptFile;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.BundledFileTemplate;
import com.intellij.ide.fileTemplates.impl.FileTemplateBase;
import com.intellij.lang.IdeLanguageCustomization;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;
//import com.jetbrains.python.PythonFileType;
//import com.tang.intellij.lua.lang.LuaFileType;
//import org.jetbrains.plugins.groovy.GroovyFileType;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowScript {

    JPanel panelMain;
    JCheckBox checkScriptAutoRun;
    JRadioButton radioScriptJavascript;
    JRadioButton radioScriptPython;
    JRadioButton radioScriptLua;
    JRadioButton radioScriptGroovy;
    JScrollPane scrollScriptSource;
    EditorEx textareaScriptSourceEditor;
    JComponent textareaScriptSource;
    JScrollPane scrollScriptResult;
    JTextArea textareaScriptResult;
    JList<ScriptFile> scriptList;
    JComponent scriptToolbar;
    ScriptModel scriptModel;

    private final Project project;
    private final ToolWindow toolWindow;

    private final Color MODIFIED_FOREGROUND = JBColor.BLUE;

    public ToolWindowScript(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }

    public JPanel getContent() {
        return this.panelMain;
    }

    @SuppressWarnings("unused")
    public Project getCurrentProject() {
        return this.project;
    }

    @SuppressWarnings("unused")
    public ToolWindow getCurrentToolWindow() {
        return this.toolWindow;
    }

    private void createUIComponents() {
        ToolWindowScriptEditorService toolWindowScriptEditorService = this.project.getService(ToolWindowScriptEditorService.class);
//        try {
//            Class<?> javaScriptFileTypeClass = Class.forName("com.intellij.lang.javascript.JavaScriptFileType");
//            Field[] fields = javaScriptFileTypeClass.getFields();
//            FileType instance = null;
//            for (Field f : fields) {
//                if (f.getType() == javaScriptFileTypeClass) {
//                    instance = (FileType) f.get(null);
//                    break;
//                }
//            }
//            this.textareaScriptSourceEditor = toolWindowScriptEditorService.newEditor("temp.js", instance, "asd");
//        } catch (ClassNotFoundException | IllegalAccessException e) {
//            NotificationUtil.warning("js class not found");
//            throw new RuntimeException(e);
//        }
        System.out.println(String.valueOf(UnknownFileType.INSTANCE));
        System.out.println(FileTypeManager.getInstance().getFileTypeByExtension("js"));
        System.out.println(FileTypeManager.getInstance().getFileTypeByExtension("java"));
        System.out.println(FileTypeManager.getInstance().getFileTypeByExtension("py"));
        System.out.println(FileTypeManager.getInstance().getFileTypeByExtension("lua"));
        System.out.println(FileTypeManager.getInstance().getFileTypeByExtension("groovy"));
        this.textareaScriptSourceEditor = toolWindowScriptEditorService.newEditor("temp.txt", PlainTextFileType.INSTANCE, "");
//        EditorFactory editorFactory = EditorFactory.getInstance();
////        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("temp.java", JavaFileType.INSTANCE, "System.out.println()", 0, true);
////        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("temp.py", PythonFileType.INSTANCE, "System.out.println()", 0, true);
////        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("temp.lua", LuaFileType.INSTANCE, "System.out.println()", 0, true);
//        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("temp.groovy", GroovyFileType.GROOVY_FILE_TYPE, "System.out.println()", 0, true);
//        Properties properties = new Properties();
//        properties.putAll(FileTemplateManager.getInstance(this.project).getDefaultProperties());
//        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "File name entered in dialog");
//        file.getViewProvider().putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, properties);
//        Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
//        if (document == null) {
//            document = EditorFactory.getInstance().createDocument("System.out.println()");
//        }
//        this.textareaScriptSourceEditor = (EditorEx) editorFactory.createEditor(document, this.project);
//        EditorSettings editorSettings = this.textareaScriptSourceEditor.getSettings();
//        editorSettings.setVirtualSpace(false);
//        editorSettings.setLineMarkerAreaShown(false);
//        editorSettings.setIndentGuidesShown(false);
//        editorSettings.setLineNumbersShown(false);
//        editorSettings.setFoldingOutlineShown(false);
//        editorSettings.setAdditionalColumnsCount(3);
//        editorSettings.setAdditionalLinesCount(3);
//        editorSettings.setCaretRowShown(false);
//        this.textareaScriptSourceEditor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
//        AutoPopupController.getInstance(this.project).scheduleAutoPopup(this.textareaScriptSourceEditor);
        this.textareaScriptSource = this.textareaScriptSourceEditor.getComponent();
//        EditorFactory.getInstance().releaseEditor(this.textareaScriptSourceEditor);
        //
//        PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("temp.java", JavaFileType.INSTANCE, "System.out.println()", 0, true);
//        Properties properties = new Properties();
//        properties.putAll(FileTemplateManager.getInstance(this.project).getDefaultProperties());
//        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "File name entered in dialog");
//        file.getViewProvider().putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, properties);
//        Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
//        this.textareaScriptSource = new EditorTextField(document, this.project, JavaFileType.INSTANCE);
        //
//        this.textareaScriptSource.addSettingsProvider(editorEx -> {
//            editorEx.setOneLineMode(false);
//            editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, new LightVirtualFile("temp.java")));
//            NotificationUtil.info("editor init done");
//            PsiFile psiFile = PsiDocumentManager.getInstance(this.project)
//                    .getPsiFile(editorEx.getDocument());
//            HighlightManager.getInstance(this.project).addRangeHighlight(editorEx, 0, 10, TextAttributesKey.createTextAttributesKey("java"), false, null);
//        });
        //
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction removeAction = new DumbAwareAction(IdeBundle.message("action.remove.template"), null, AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ScriptFile selectedValue = scriptList.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                int selectedIndex = scriptList.getSelectedIndex();
                scriptModel.remove(selectedIndex);
                if (!scriptModel.isEmpty()) {
                    scriptList.setSelectedIndex(Math.min(selectedIndex, scriptModel.size() - 1));
                }
                onScriptSelected(null);
//                isModified = true;
            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(scriptList.getSelectedValue() != null);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        AnAction addAction = new DumbAwareAction(IdeBundle.message("action.create.template"), null, AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // TODO 需要一个弹窗输入文件名、文件类型
//                String ext = StringUtil.notNullize(JBIterable.from(IdeLanguageCustomization.getInstance().getPrimaryIdeLanguages())
//                        .filterMap(Language::getAssociatedFileType)
//                        .filterMap(FileType::getDefaultExtension)
//                        .first(), "txt");
//                FileTemplateBase selected = ObjectUtils.tryCast(getSelectedTemplate(), FileTemplateBase.class);
//                if (selected == null && false) return;
//                String name = false ? selected.getChildName(selected.getChildren().length) : IdeBundle.message("template.unnamed");
//                FileTemplate template = createTemplate(name, ext, "", child);
//                if (false) {
//                    selected.addChild(template);
//                }
                ScriptFile scriptFile = new ScriptFile();
                scriptFile.setFileName("test");
                scriptFile.setFileSuffix("java");
                scriptFile.setScript("abc");
                scriptModel.addElement(scriptFile);
//                isModified = true;
//                fireListChanged();
                scriptModel.fireListDataChanged();
//                editor.focusToNameField();
//                JComponent field = FileTemplateBase.isChild(myTemplate) ? myFileName : myNameField;
//                myNameField.selectAll();
//                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(field, true));
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(textareaScriptSource, true));
            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(true);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
//        AnAction addChildAction = new DumbAwareAction(IdeBundle.message("action.create.child.template"), null, AllIcons.Actions.AddFile) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e) {
//                onAdd(true);
//            }
//            @Override
//            public void update(@NotNull AnActionEvent e) {
//                e.getPresentation().setEnabled(getSelectedTemplate() != null &&
//                        currentTab != null &&
//                        !isInternalTemplate(getSelectedTemplate().getName(), currentTab.getTitle()) &&
//                        !FileTemplateBase.isChild(getSelectedTemplate()) &&
//                        currentTab == myTemplatesList);
//            }
//            @Override
//            public @NotNull ActionUpdateThread getActionUpdateThread() {
//                return ActionUpdateThread.EDT;
//            }
//        };
        AnAction cloneAction = new DumbAwareAction(IdeBundle.message("action.copy.template"), null, PlatformIcons.COPY_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ScriptFile selectedValue = scriptList.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                ScriptFile clone = selectedValue.clone();
                scriptModel.addElement(clone);
                scriptModel.fireListDataChanged();
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(textareaScriptSource, true));
            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(scriptList.getSelectedValue() != null);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
//        AnAction resetAction = new DumbAwareAction(IdeBundle.message("action.reset.to.default"), null, AllIcons.Actions.Rollback) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e) {
//                onReset();
//            }
//
//            @Override
//            public void update(@NotNull AnActionEvent e) {
//                if (currentTab == null) {
//                    e.getPresentation().setEnabled(false);
//                    return;
//                }
//                final FileTemplate selectedItem = getSelectedTemplate();
//                e.getPresentation().setEnabled(selectedItem instanceof BundledFileTemplate && !selectedItem.isDefault());
//            }
//            @Override
//            public @NotNull ActionUpdateThread getActionUpdateThread() {
//                return ActionUpdateThread.EDT;
//            }
//        };
        group.add(addAction);
//        group.add(addChildAction);
        group.add(removeAction);
        group.add(cloneAction);
//        group.add(resetAction);
        // place group horizontal
        this.scriptToolbar = ActionManager.getInstance().createActionToolbar("FileTemplatesConfigurable", group, true).getComponent();
        this.scriptToolbar.setBorder(new CustomLineBorder(1, 1, 0, 1));
        //
        this.scriptList = new JBList<>();
        this.scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.scriptList.setCellRenderer(SimpleListCellRenderer.create((label, value, index) -> {
            label.setIcon(value.getFileType().getIcon());
            label.setText(value.getFileName());
            // TODO 校验是否被修改
//            if (!value.isDefault() && myList.getSelectedIndex() != index) {
//                label.setForeground(MODIFIED_FOREGROUND);
//            }
        }));
        this.scriptList.addListSelectionListener(this::onScriptSelected);
        new ListSpeedSearch<>(this.scriptList, ScriptFile::getFileName);
        this.scriptModel = new ScriptModel();
        this.scriptList.setModel(this.scriptModel);
        {
            ScriptFile scriptFile = new ScriptFile();
            scriptFile.setFileName("test");
            scriptFile.setFileSuffix("java");
            scriptFile.setScript("abc");
            this.scriptModel.addElement(scriptFile);
        }
    }

    // same as com.intellij.ide.fileTemplates.impl.AllFileTemplatesConfigurable.onListSelectionChanged
    public void onScriptSelected(ListSelectionEvent event) {
        // TODO 卸载旧的Document，加载新的Document
        System.out.println("script selected " + this.scriptList.getSelectedValue() + " " + event);
    }

    private static class ScriptModel extends DefaultListModel<ScriptFile> {
        // Copy from com.intellij.ide.fileTemplates.impl.FileTemplateTabAsList.MyListModel.fireListDataChanged
        private void fireListDataChanged() {
            int size = getSize();
            if (size > 0) {
                fireContentsChanged(this, 0, size - 1);
            }
        }
    }

}
