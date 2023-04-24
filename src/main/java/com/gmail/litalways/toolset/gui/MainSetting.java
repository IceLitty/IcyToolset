package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.state.MainSettingState;
import com.gmail.litalways.toolset.state.MainSettingsClassName;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PlatformIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单/复杂设置样例使用的窗口
 *
 * @author IceRain
 * @since 2023/04/06
 */
@Getter
public class MainSetting {

    JPanel panelMain;
    JComponent beanUtilsToolbar;
    JBTable beanUtilsClassTable;

    final MainSettingState settingObject;

    public MainSetting(MainSettingState settingObject) {
        this.settingObject = settingObject;
    }

    @SuppressWarnings("unused")
    public JPanel getContent() {
        return this.panelMain;
    }

    public void createUIComponents() {
        // 生成表格
        String[][] row = new String[settingObject.beanUtilsClassName.size()][];
        for (int i = 0, len = settingObject.beanUtilsClassName.size(); i < len; i++) {
            MainSettingsClassName bean = settingObject.beanUtilsClassName.get(i);
            String[] col = new String[3];
            col[0] = bean.getSimpleClassName();
            col[1] = bean.getQualifierClassName();
            col[2] = bean.getMethodName();
            row[i] = col;
        }
        String[] col = new String[]{
                MessageUtil.getMessage("setting.main.bean.utils.table.column.simple.class.name.title"),
                MessageUtil.getMessage("setting.main.bean.utils.table.column.full.class.name.title"),
                MessageUtil.getMessage("setting.main.bean.utils.table.column.method.title")
        };
        beanUtilsClassTable = new JBTable(new DefaultTableModel(row, col));
        // 生成按钮组
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction addAction = new DumbAwareAction(MessageUtil.getMessage("setting.main.bean.utils.action.add.tooltip"), null, AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                MainSettingsClassName className = new MainSettingsClassName("", "", "");
                MainSettingState.getInstance().beanUtilsClassName.add(className);
                String[] row = new String[]{"", "", ""};
                ((DefaultTableModel) beanUtilsClassTable.getModel()).addRow(row);
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
        AnAction removeAction = new DumbAwareAction(MessageUtil.getMessage("setting.main.bean.utils.action.remove.tooltip"), null, AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int[] indexes = beanUtilsClassTable.getSelectedRows();
                DefaultTableModel tableModel = (DefaultTableModel) beanUtilsClassTable.getModel();
                for (int i = indexes.length - 1; i >= 0; i--) {
                    tableModel.removeRow(indexes[i]);
                    MainSettingState.getInstance().beanUtilsClassName.remove(indexes[i]);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(beanUtilsClassTable.getSelectedRowCount() > 0);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        AnAction cloneAction = new DumbAwareAction(MessageUtil.getMessage("setting.main.bean.utils.action.copy.tooltip"), null, PlatformIcons.COPY_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int index = beanUtilsClassTable.getSelectedRow();
                DefaultTableModel tableModel = (DefaultTableModel) beanUtilsClassTable.getModel();
                String qualifierClassName = (String) tableModel.getValueAt(index, 0);
                String fullClassName = (String) tableModel.getValueAt(index, 1);
                String methodName = (String) tableModel.getValueAt(index, 2);
                MainSettingsClassName className = new MainSettingsClassName(qualifierClassName, fullClassName, methodName);
                MainSettingState.getInstance().beanUtilsClassName.add(className);
                String[] row = new String[]{qualifierClassName, fullClassName, methodName};
                ((DefaultTableModel) beanUtilsClassTable.getModel()).addRow(row);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(beanUtilsClassTable.getSelectedRowCount() == 1);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        group.add(addAction);
        group.add(removeAction);
        group.add(cloneAction);
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, group, false);
        actionToolbar.setTargetComponent(this.beanUtilsClassTable);
        this.beanUtilsToolbar = actionToolbar.getComponent();
        this.beanUtilsToolbar.setBorder(new CustomLineBorder(1, 1, 0, 1));
    }

    public List<MainSettingsClassName> getTableDataValue() {
        List<MainSettingsClassName> list = new ArrayList<>();
        DefaultTableModel tableModel = (DefaultTableModel) beanUtilsClassTable.getModel();
        var dataVector = tableModel.getDataVector();
        for (var v : dataVector) {
            MainSettingsClassName className = new MainSettingsClassName();
            className.setSimpleClassName(v.get(0) == null ? "" : String.valueOf(v.get(0)));
            className.setQualifierClassName(v.get(1) == null ? "" : String.valueOf(v.get(1)));
            className.setMethodName(v.get(2) == null ? "" : String.valueOf(v.get(2)));
            list.add(className);
        }
        return list;
    }

    public void loadTableData(List<MainSettingsClassName> data) {
        DefaultTableModel tableModel = (DefaultTableModel) beanUtilsClassTable.getModel();
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
        for (MainSettingsClassName c : data) {
            String[] row = new String[]{c.getSimpleClassName(), c.getQualifierClassName(), c.getMethodName()};
            tableModel.addRow(row);
        }
    }

}
