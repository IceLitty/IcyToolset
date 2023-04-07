package com.gmail.litalways.toolset.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * @author IceRain
 * @since 2022/01/19
 */
public class MainFormFactory implements ToolWindowFactory {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("formTitle");

    @Override
    @SuppressWarnings("unused")
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        //
        ToolWindowConvert toolWindowConvert = new ToolWindowConvert(project, toolWindow);
        // createContent(component, displayName, isLockable) https://dploeger.github.io/intellij-api-doc/com/intellij/ui/content/ContentFactory.html
        Content toolWindowConvertContent = contentFactory.createContent(toolWindowConvert.getContent(), messageBundle.getString("tab.convert.title"), true);
        toolWindow.getContentManager().addContent(toolWindowConvertContent);
        MainFormConvertCommonFunction convertCommon = new MainFormConvertCommonFunction(toolWindowConvert);
        MainFormConvertImgBase64Function convertImgBase64 = new MainFormConvertImgBase64Function(toolWindowConvert);
        MainFormConvertSplitterFunction convertSplitter = new MainFormConvertSplitterFunction(toolWindowConvert);
        //
        ToolWindowEncrypt toolWindowEncrypt = new ToolWindowEncrypt(project, toolWindow);
        Content toolWindowEncryptContent = contentFactory.createContent(toolWindowEncrypt.getContent(), messageBundle.getString("tab.encrypt.title"), false);
        toolWindow.getContentManager().addContent(toolWindowEncryptContent);
        MainFormEncryptHashFunction encryptHash = new MainFormEncryptHashFunction(toolWindowEncrypt);
        MainFormEncryptAsymmetricFunction encryptAsymmetric = new MainFormEncryptAsymmetricFunction(toolWindowEncrypt);
        MainFormEncryptSymmetricFunction encryptSymmetric = new MainFormEncryptSymmetricFunction(toolWindowEncrypt);
        toolWindowEncryptContent.setCloseable(true);
        //
        ToolWindowQRCode toolWindowQRCode = new ToolWindowQRCode(project, toolWindow);
        Content toolWindowQRCodeContent = contentFactory.createContent(toolWindowQRCode.getContent(), messageBundle.getString("tab.zxing.title"), false);
        toolWindow.getContentManager().addContent(toolWindowQRCodeContent);
        MainFormZxingFunction zxing = new MainFormZxingFunction(toolWindowQRCode);
        //
        ToolWindowFormat toolWindowFormat = new ToolWindowFormat(project, toolWindow);
        Content toolWindowFormatContent = contentFactory.createContent(toolWindowFormat.getContent(), messageBundle.getString("tab.format.title"), false);
        toolWindow.getContentManager().addContent(toolWindowFormatContent);
        MainFormFormatFunction format = new MainFormFormatFunction(toolWindowFormat);
        //
        ToolWindowScript toolWindowScript = new ToolWindowScript(project, toolWindow);
        Content toolWindowScriptContent = contentFactory.createContent(toolWindowScript.getContent(), messageBundle.getString("tab.script.title"), false);
        toolWindow.getContentManager().addContent(toolWindowScriptContent);
        MainFormScriptFunction script = new MainFormScriptFunction(toolWindowScript);
        //
        ToolWindowSymbol toolWindowSymbol = new ToolWindowSymbol(project, toolWindow);
        Content toolWindowSymbolContent = contentFactory.createContent(toolWindowSymbol.getContent(), messageBundle.getString("tab.symbol.title"), false);
        toolWindow.getContentManager().addContent(toolWindowSymbolContent);
        //
        ToolWindowOther toolWindowOther = new ToolWindowOther(project, toolWindow);
        Content toolWindowOtherContent = contentFactory.createContent(toolWindowOther.getContent(), messageBundle.getString("tab.about.title"), false);
        toolWindow.getContentManager().addContent(toolWindowOtherContent);
        MainFormAboutFunction about = new MainFormAboutFunction(toolWindowOther);
    }

}
