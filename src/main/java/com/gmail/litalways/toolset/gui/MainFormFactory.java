package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.config.MainSettingStateConfigurable;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
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
        ContentFactory contentFactory = ContentFactory.getInstance();
        // 转换
        ToolWindowConvert toolWindowConvert = new ToolWindowConvert(project, toolWindow);
        // createContent(component, displayName, isLockable) https://dploeger.github.io/intellij-api-doc/com/intellij/ui/content/ContentFactory.html
        Content toolWindowConvertContent = contentFactory.createContent(toolWindowConvert.getContent(), MessageUtil.getMessage("convert.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowConvertContent);
        MainFormConvertCommonFunction convertCommon = new MainFormConvertCommonFunction(toolWindowConvert);
        MainFormConvertImgBase64Function convertImgBase64 = new MainFormConvertImgBase64Function(toolWindowConvert);
        MainFormConvertSplitterFunction convertSplitter = new MainFormConvertSplitterFunction(toolWindowConvert);
        // 加解密
        ToolWindowEncrypt toolWindowEncrypt = new ToolWindowEncrypt(project, toolWindow);
        Content toolWindowEncryptContent = contentFactory.createContent(toolWindowEncrypt.getContent(), MessageUtil.getMessage("encrypt.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowEncryptContent);
        MainFormEncryptHashFunction encryptHash = new MainFormEncryptHashFunction(toolWindowEncrypt);
        MainFormEncryptAsymmetricFunction encryptAsymmetric = new MainFormEncryptAsymmetricFunction(project, toolWindowEncrypt);
        MainFormEncryptSymmetricFunction encryptSymmetric = new MainFormEncryptSymmetricFunction(toolWindowEncrypt);
        MainFormEncryptJWTFunction encryptJWT = new MainFormEncryptJWTFunction(toolWindowEncrypt);
        toolWindowEncryptContent.setCloseable(false);
        // 扫码
        ToolWindowQRCode toolWindowQRCode = new ToolWindowQRCode(project, toolWindow);
        Content toolWindowQRCodeContent = contentFactory.createContent(toolWindowQRCode.getContent(), MessageUtil.getMessage("qr.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowQRCodeContent);
        MainFormQRFunction zxing = new MainFormQRFunction(toolWindowQRCode);
        // 格式化
        ToolWindowFormat toolWindowFormat = new ToolWindowFormat(project, toolWindow);
        Content toolWindowFormatContent = contentFactory.createContent(toolWindowFormat.getContent(), MessageUtil.getMessage("format.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowFormatContent);
        MainFormFormatFunction format = new MainFormFormatFunction(toolWindowFormat);
        // 脚本
        ToolWindowScript toolWindowScript = new ToolWindowScript(project, toolWindow);
        Content toolWindowScriptContent = contentFactory.createContent(toolWindowScript.getContent(), MessageUtil.getMessage("script.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowScriptContent);
        MainFormScriptFunction script = new MainFormScriptFunction(toolWindowScript);
        // 符号
        ToolWindowSymbol toolWindowSymbol = new ToolWindowSymbol(project, toolWindow);
        Content toolWindowSymbolContent = contentFactory.createContent(toolWindowSymbol.getContent(), MessageUtil.getMessage("symbol.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowSymbolContent);
        // 其他
        ToolWindowOther toolWindowOther = new ToolWindowOther(project, toolWindow);
        Content toolWindowOtherContent = contentFactory.createContent(toolWindowOther.getContent(), MessageUtil.getMessage("other.tab.title"), true);
        toolWindow.getContentManager().addContent(toolWindowOtherContent);
        MainFormOtherFunction about = new MainFormOtherFunction(toolWindowOther);
        // 靠右按钮
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction openSettingsAction = new DumbAwareAction(MessageUtil.getMessage("action.toolbar.open.setting.title"), null, AllIcons.General.Settings) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, MainSettingStateConfigurable.class);
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
        group.add(openSettingsAction);
        toolWindow.setAdditionalGearActions(group);
    }

}
