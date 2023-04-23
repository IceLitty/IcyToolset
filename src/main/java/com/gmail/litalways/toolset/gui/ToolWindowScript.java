package com.gmail.litalways.toolset.gui;

import cn.hutool.script.ScriptUtil;
import com.gmail.litalways.toolset.listener.ScrollbarSyncListener;
import com.gmail.litalways.toolset.service.ToolWindowScriptEditorService;
import com.gmail.litalways.toolset.state.ScriptFile;
import com.gmail.litalways.toolset.state.ToolWindowScriptState;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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
    JPanel scrollScriptSource;
    EditorEx textareaScriptSourceEditor;
    JComponent textareaScriptSource;
    ScrollbarSyncListener syncListener;
    JScrollPane scrollScriptResult;
    JTextArea textareaScriptResult;
    JList<ScriptFile> scriptList;
    JComponent scriptToolbar;
    JTextField textScriptFilename;
    ScriptModel scriptModel;
    AtomicInteger injectedNashorn;

    private final Project project;
    private final ToolWindow toolWindow;

    @SuppressWarnings("unused")
    private final Color MODIFIED_FOREGROUND = JBColor.BLUE;

    public ToolWindowScript(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.injectedNashorn = new AtomicInteger(0);
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

    @SuppressWarnings("CommentedOutCode")
    private void createUIComponents() {
        ToolWindowScriptEditorService toolWindowScriptEditorService = this.project.getService(ToolWindowScriptEditorService.class);
        // 生成空文本默认编辑器
        this.textareaScriptSourceEditor = toolWindowScriptEditorService.newEditor("dummy.txt", PlainTextFileType.INSTANCE, "");
        this.textareaScriptSource = this.textareaScriptSourceEditor.getComponent();
        // 生成按钮组
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction addAction = new DumbAwareAction(MessageUtil.getMessage("script.action.add.tooltip"), null, AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ScriptFile scriptFile = new ScriptFile();
                scriptFile.setFileName("temp_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
                scriptFile.setFileSuffix("js");
                scriptFile.setScript("");
                ToolWindowScriptState.getInstance().scriptFiles.add(scriptFile);
                scriptModel.addElement(scriptFile);
                scriptModel.fireListDataChanged();
                IdeFocusManager.getInstance(project).requestFocus(textareaScriptSourceEditor.getContentComponent(), true);
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
        AnAction removeAction = new DumbAwareAction(MessageUtil.getMessage("script.action.remove.tooltip"), null, AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ScriptFile selectedValue = scriptList.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                int selectedIndex = scriptList.getSelectedIndex();
                scriptModel.remove(selectedIndex);
                ToolWindowScriptState.getInstance().scriptFiles.removeIf(s -> s == selectedValue);
                if (!scriptModel.isEmpty()) {
                    scriptList.setSelectedIndex(Math.min(selectedIndex, scriptModel.size() - 1));
                }
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
        AnAction cloneAction = new DumbAwareAction(MessageUtil.getMessage("script.action.copy.tooltip"), null, PlatformIcons.COPY_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ScriptFile selectedValue = scriptList.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                ScriptFile clone = selectedValue.clone();
                if (Pattern.matches("^temp_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}$", clone.getFileName())) {
                    clone.setFileName("temp_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
                } else {
                    clone.setFileName(clone.getFileName() + "_copy");
                }
                ToolWindowScriptState.getInstance().scriptFiles.add(clone);
                scriptModel.addElement(clone);
                scriptModel.fireListDataChanged();
                IdeFocusManager.getInstance(project).requestFocus(textareaScriptSourceEditor.getContentComponent(), true);
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
        group.add(addAction);
        group.add(removeAction);
        group.add(cloneAction);
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, group, true);
        this.scriptToolbar = actionToolbar.getComponent();
        this.scriptToolbar.setBorder(new CustomLineBorder(1, 1, 0, 1));
        // 生成列表
        this.scriptList = new JBList<>();
        actionToolbar.setTargetComponent(scriptList);
        this.scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.scriptList.setCellRenderer(SimpleListCellRenderer.create((label, value, index) -> {
            label.setIcon(value.getFileType().getIcon());
            label.setText(value.getFileName());
            // （不实现该功能）校验是否被修改
//            if (!value.isDefault() && myList.getSelectedIndex() != index) {
//                label.setForeground(MODIFIED_FOREGROUND);
//            }
        }));
        this.scriptList.addListSelectionListener(this::onScriptSelected);
        new ListSpeedSearch<>(this.scriptList, ScriptFile::getFileName);
        this.scriptModel = new ScriptModel();
        this.scriptList.setModel(this.scriptModel);
        if (ToolWindowScriptState.getInstance().scriptFiles != null) {
            for (ScriptFile scriptFile : ToolWindowScriptState.getInstance().scriptFiles) {
                this.scriptModel.addElement(scriptFile);
            }
        }
    }

    // same as com.intellij.ide.fileTemplates.impl.AllFileTemplatesConfigurable.onListSelectionChanged
    public void onScriptSelected(ListSelectionEvent event) {
        ScriptFile selectedValue = this.scriptList.getSelectedValue();
        if (event != null && event.getValueIsAdjusting()) {
            return;
        }
        ToolWindowScriptEditorService toolWindowScriptEditorService = this.project.getService(ToolWindowScriptEditorService.class);
        // cmp address
        if (selectedValue == toolWindowScriptEditorService.getLastScriptFile()) {
            return;
        }
        toolWindowScriptEditorService.setLastScriptFile(selectedValue);
        // 填充文件名文本框及单选按钮
        if (selectedValue == null) {
            this.textScriptFilename.setText("");
            this.radioScriptJavascript.setSelected(false);
            this.radioScriptPython.setSelected(false);
            this.radioScriptLua.setSelected(false);
            this.radioScriptGroovy.setSelected(false);
        } else {
            this.textScriptFilename.setText(selectedValue.getFileName());
            this.radioScriptJavascript.setSelected("js".equalsIgnoreCase(selectedValue.getFileSuffix()));
            this.radioScriptPython.setSelected("py".equalsIgnoreCase(selectedValue.getFileSuffix()));
            this.radioScriptLua.setSelected("lua".equalsIgnoreCase(selectedValue.getFileSuffix()));
            this.radioScriptGroovy.setSelected("groovy".equalsIgnoreCase(selectedValue.getFileSuffix()));
        }
        // 卸载旧的Editor，加载新的Editor
        useCurrentStateToCreateEditor();
    }

    void radioChanged(ActionEvent e) {
        JRadioButton radioButton = (JRadioButton) e.getSource();
        if (radioButton.isSelected()) {
            ScriptFile selectedValue = this.scriptList.getSelectedValue();
            if (selectedValue == null) {
                return;
            }
            ToolWindowScriptEditorService toolWindowScriptEditorService = this.getCurrentProject().getService(ToolWindowScriptEditorService.class);
            ScriptFile lastScriptFile = toolWindowScriptEditorService.getLastScriptFile();
            if (this.radioScriptJavascript.isSelected()) {
                selectedValue.setFileSuffix("js");
            } else if (this.radioScriptLua.isSelected()) {
                selectedValue.setFileSuffix("lua");
            } else if (this.radioScriptGroovy.isSelected()) {
                selectedValue.setFileSuffix("groovy");
            } else if (this.radioScriptPython.isSelected()) {
                selectedValue.setFileSuffix("py");
            }
            if (lastScriptFile == null || selectedValue.getFileSuffix().equals(lastScriptFile.getFileSuffix())) {
                // 卸载旧的Editor，加载新的Editor
                useCurrentStateToCreateEditor();
                ToolWindowScriptState.getInstance().scriptFiles.removeIf(s -> s == selectedValue);
                ToolWindowScriptState.getInstance().scriptFiles.add(selectedValue);
                this.scriptModel.fireListDataChanged();
            }
            if (this.checkScriptAutoRun.isSelected()) {
                ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("script.tip.running")) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        eval();
                    }
                });
            }
        }
    }

    private void useCurrentStateToCreateEditor() {
        ScriptFile selectedValue = this.scriptList.getSelectedValue();
        ToolWindowScriptEditorService toolWindowScriptEditorService = this.getCurrentProject().getService(ToolWindowScriptEditorService.class);
        this.scrollScriptResult.getVerticalScrollBar().removeAdjustmentListener(this.syncListener);
        this.scrollScriptResult.getHorizontalScrollBar().removeAdjustmentListener(this.syncListener);
        this.scrollScriptSource.remove(this.textareaScriptSource);
        toolWindowScriptEditorService.disposed();
        if (selectedValue == null) {
            this.textareaScriptSourceEditor = toolWindowScriptEditorService.newEditor("dummy.txt", PlainTextFileType.INSTANCE, "");
        } else {
            this.textareaScriptSourceEditor = toolWindowScriptEditorService.newEditor(selectedValue.getFileName() + "." + selectedValue.getFileSuffix(), selectedValue.getFileType(), selectedValue.getScript());
            this.textareaScriptSourceEditor.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    ScriptFile selectedValue = scriptList.getSelectedValue();
                    if (selectedValue != null) {
                        selectedValue.setScript(textareaScriptSourceEditor.getDocument().getText());
                        ToolWindowScriptState.getInstance().scriptFiles.removeIf(s -> s == selectedValue);
                        ToolWindowScriptState.getInstance().scriptFiles.add(selectedValue);
                        scriptModel.fireListDataChanged();
                    }
                    if (checkScriptAutoRun.isSelected()) {
                        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("script.tip.running")) {
                            @Override
                            public void run(@NotNull ProgressIndicator progressIndicator) {
                                eval();
                            }
                        });
                    }
                }
            });
        }
        this.textareaScriptSource = this.textareaScriptSourceEditor.getComponent();
        this.scrollScriptSource.add(this.textareaScriptSource, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, new Dimension(-1, -1), new Dimension(-1, -1), new Dimension(-1, -1), 0, true));
        this.scrollScriptSource.validate();
        this.scrollScriptSource.repaint();
        this.syncListener = new ScrollbarSyncListener(this.textareaScriptSourceEditor.getScrollPane(), this.scrollScriptResult);
        this.textareaScriptSourceEditor.getScrollPane().getVerticalScrollBar().addAdjustmentListener(this.syncListener);
        this.textareaScriptSourceEditor.getScrollPane().getHorizontalScrollBar().addAdjustmentListener(this.syncListener);
    }

    void eval() {
        try {
            if (this.injectedNashorn.get() != 2) {
                return;
            }
            String script = this.textareaScriptSourceEditor.getDocument().getText();
            if (this.radioScriptJavascript.isSelected()) {
                this.textareaScriptResult.setText(String.valueOf(ScriptUtil.getJsEngine().eval(script)));
            } else if (this.radioScriptLua.isSelected()) {
                this.textareaScriptResult.setText(String.valueOf(ScriptUtil.getLuaEngine().eval(script)));
            } else if (this.radioScriptGroovy.isSelected()) {
                this.textareaScriptResult.setText(String.valueOf(ScriptUtil.getGroovyEngine().eval(script)));
            } else if (this.radioScriptPython.isSelected()) {
                String[] scripts = script.split("\n\n");
                ScriptEngine pythonEngine = ScriptUtil.getPythonEngine();
                StringBuilder builder = new StringBuilder();
                for (String s : scripts) {
                    builder.append(pythonEngine.eval(s)).append("\n\n");
                }
                this.textareaScriptResult.setText(builder.toString());
            } else {
                NotificationUtil.warning(MessageUtil.getMessage("script.tip.not.select.type"));
            }
        } catch (Exception ex) {
            this.textareaScriptResult.setText(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
        }
    }

    // same as com.intellij.ide.fileTemplates.impl.FileTemplateTabAsList.MyListModel.fireListDataChanged
    static class ScriptModel extends DefaultListModel<ScriptFile> {
        void fireListDataChanged() {
            int size = this.getSize();
            if (size > 0) {
                this.fireContentsChanged(this, 0, size - 1);
            }
        }
    }

}
