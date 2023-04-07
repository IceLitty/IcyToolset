package com.gmail.litalways.toolset.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 简单菜单事件
 *
 * @author IceRain
 * @since 2023/04/07
 */
@SuppressWarnings("ComponentNotRegistered")
public class SimpleAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (new SimpleDialog(project).showAndGet()) {
            System.out.println("User select ok.");
        } else {
            System.out.println("User select cancel.");
        }
    }

    static class SimpleDialog extends DialogWrapper {

        public SimpleDialog(@Nullable Project project) {
            super(project);
            setTitle("Test Dialog");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Testing");
//            label.setPreferredSize(new Dimension(100, 100));
            dialogPanel.add(label, BorderLayout.CENTER);
            return dialogPanel;
        }

    }

}
