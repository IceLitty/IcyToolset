package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author IceRain
 * @since 2022/01/19
 */
public class MainFormFactory implements ToolWindowFactory {

    @Override
    @SuppressWarnings("unused")
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MainForm mainForm = new MainForm(project, toolWindow);
        // TODO 此处需要优化将各个Form拆分开来，以IDEA自带的TAB模式进行显示，少占一行空间
        // createContent(component, displayName, isLockable) https://dploeger.github.io/intellij-api-doc/com/intellij/ui/content/ContentFactory.html
        Content content = ContentFactory.getInstance().createContent(mainForm.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
        // 调用注册方法
        MainFormConvertCommonFunction convertCommon = new MainFormConvertCommonFunction(mainForm);
        MainFormConvertImgBase64Function convertImgBase64 = new MainFormConvertImgBase64Function(mainForm);
        MainFormConvertSplitterFunction convertSplitter = new MainFormConvertSplitterFunction(mainForm);
        MainFormEncryptHashFunction encryptHash = new MainFormEncryptHashFunction(mainForm);
        MainFormEncryptAsymmetricFunction encryptAsymmetric = new MainFormEncryptAsymmetricFunction(mainForm);
        MainFormEncryptSymmetricFunction encryptSymmetric = new MainFormEncryptSymmetricFunction(mainForm);
        MainFormZxingFunction zxing = new MainFormZxingFunction(mainForm);
        MainFormFormatFunction format = new MainFormFormatFunction(mainForm);
        MainFormScriptFunction script = new MainFormScriptFunction(mainForm);
        MainFormAboutFunction about = new MainFormAboutFunction(mainForm);
    }

}
