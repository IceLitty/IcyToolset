package com.gmail.litalways.toolset.action;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.stream.StreamUtil;
import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.exception.ProxyException;
import com.gmail.litalways.toolset.util.ClassVersionUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.gmail.litalways.toolset.util.PdfGenerateUtil;
import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 批量检测项目依赖所需java版本（如用于java8项目检测是否依赖需要java9及以上）
 *
 * @author IceRain
 * @since 2024/8/1
 */
@Slf4j
public class ProjectViewFindDependencyJavaVersionAction extends AnAction {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("message");

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.usage.by.bean.utils.searching")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // BGT
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT with read access
                    Module[] modules = ModuleManager.getInstance(project).getModules();
                    DependencyCheckResult result = new DependencyCheckResult();
                    for (Module module : modules) {
                        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                        Sdk sdk = moduleRootManager.getSdk();
                        JavaSdkVersion jdkVersion;
                        if (sdk != null && sdk.getSdkType() instanceof JavaSdk jdk && (jdkVersion = jdk.getVersion(sdk)) != null) {
                            int feature = jdkVersion.getMaxLanguageLevel().toJavaVersion().feature;
                            VirtualFile[] classesRoots = moduleRootManager.orderEntries().librariesOnly().getClassesRoots();
                            for (VirtualFile dependency : classesRoots) {
                                String fileKey = dependency.getUrl();
                                boolean found = false;
                                for (DependencyCheckResult.Dependency resultDependency : result.getDependencies()) {
                                    if (resultDependency.getUrl().equals(fileKey)) {
                                        found = true;
                                        resultDependency.getModules().put(module.getName(), feature);
                                        break;
                                    }
                                }
                                if (found) {
                                    continue;
                                }
                                DependencyCheckResult.Dependency resultDependency = new DependencyCheckResult.Dependency();
                                resultDependency.setUrl(fileKey);
                                resultDependency.getModules().put(module.getName(), feature);
                                result.getDependencies().add(resultDependency);
                                if (dependency.getFileType() instanceof ArchiveFileType) {
                                    URI uri = URI.create("jar:" + new File(dependency.getPath().replace("!/", "")).toURI());
                                    try (FileSystem zipFs = FileSystems.newFileSystem(uri, new HashMap<>());) {
                                        try (Stream<Path> walk = Files.walk(zipFs.getPath("/"))
                                                .filter(Files::isRegularFile)
                                                .filter(f -> f.getFileName().toString().endsWith(".class"));) {
                                            for (Iterator<Path> iterator = walk.iterator(); iterator.hasNext();) {
                                                Path pathNode = iterator.next();
                                                String name = pathNode.toString();
                                                Path path = zipFs.getPath(name);
                                                // read
                                                try (InputStream inputStream = Files.newInputStream(path);
                                                     DataInputStream dataInputStream = new DataInputStream(inputStream);) {
                                                    checkClassFile(feature, name, dataInputStream, resultDependency);
                                                }
                                            }
                                        }
                                    } catch (IOException ex) {
                                        resultDependency.setReadError(MessageUtil.getMessage("action.find.dependency.java.version.file.system.error", fileKey, ex.getLocalizedMessage()));
                                    }
                                } else if (dependency.getFileType() instanceof JavaClassFileType) {
                                    try (InputStream inputStream = dependency.getInputStream();
                                         DataInputStream dataInputStream = new DataInputStream(inputStream);) {
                                        checkClassFile(feature, dependency.getPath(), dataInputStream, resultDependency);
                                    } catch (IOException ex) {
                                        resultDependency.setReadError(MessageUtil.getMessage("action.find.dependency.java.version.file.system.error", fileKey, ex.getLocalizedMessage()));
                                    }
                                } else {
                                    resultDependency.setSkip(true);
                                }
                            }
                        }
                    }
                    // output
                    ApplicationManager.getApplication().invokeLater(() -> {
                        // EDT
                        int sum = result.getDependencies().stream().mapToInt(d -> d.getReadError() != null ? 1 : d.getNotSupportFiles().size()).sum();
                        NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup(KeyConstant.NOTIFICATION_GROUP_KEY);
                        group.createNotification(MessageUtil.getMessage("action.find.dependency.java.version.done", modules.length, result.getDependencies().size(), sum), NotificationType.INFORMATION)
                                .addAction(new BalloonActionCopyToClipboard(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonActionCopyToClipboard.text"), result))
                                .addAction(new BalloonActionSaveToFile(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonActionSaveToFile.text"), result))
                                .notify(null);
                    });
                });
            }
        });
    }

    private void checkClassFile(int moduleJdkVersion, String fileName, DataInputStream dataInputStream, DependencyCheckResult.Dependency resultDependency) throws IOException {
        int magic = dataInputStream.readInt();
        if (magic != 0xCAFEBABE) {
            resultDependency.getClassNotBeReadFiles().add(fileName);
            return;
        }
        // TODO if filename spilt by / and name equals module-info.class or package-info.class, and if jdk is 8, ignored
        //      if filename startwith /META-INF/ and not support, may lib use dynamic check to load impl class, don't be not supported
        int minor = dataInputStream.readUnsignedShort();
        int major = dataInputStream.readUnsignedShort();
        Boolean capability = ClassVersionUtil.checkCapability(moduleJdkVersion, major);
        boolean guess = false;
        if (capability == null) {
            guess = true;
            capability = ClassVersionUtil.guessCapability(moduleJdkVersion, major);
        }
        if (capability) {
            resultDependency.getSupportFiles().add(new DependencyCheckResult.CheckedFile(fileName, major, guess));
        } else {
            resultDependency.getNotSupportFiles().add(new DependencyCheckResult.CheckedFile(fileName, major, guess));
        }
    }

    @Slf4j
    private static class BalloonActionCopyToClipboard extends AnAction {
        private final DependencyCheckResult result;
        public BalloonActionCopyToClipboard(@Nullable @NlsActions.ActionText String text, DependencyCheckResult result) {
            super(text);
            this.result = result;
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            // TODO BGT
            // TODO output to clipboard
            NotificationUtil.info("111111", "11111111111");
        }
    }

    @Slf4j
    private static class BalloonActionSaveToFile extends AnAction {
        private final DependencyCheckResult result;
        public BalloonActionSaveToFile(@Nullable @NlsActions.ActionText String text, DependencyCheckResult result) {
            super(text);
            this.result = result;
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            try {
                // TODO BGT
                String base64 = generatePdf(result);
                // TODO output to file
                System.out.println("");
            } catch (ProxyException e) {
                NotificationUtil.info(e.getLocalizedMessage());
            }
        }
    }

    private static String generatePdf(DependencyCheckResult result) throws ProxyException {
        try {
            String jrXml;
            //noinspection DataFlowIssue
            try (InputStream inputStream = ProjectViewFindDependencyJavaVersionAction.class.getClassLoader()
                    .getResourceAsStream("report/IcyToolsetAnalyzeDependenciesReport.jrxml");
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);) {
                StringBuilder builder = new StringBuilder();
                int len;
                char[] cache = new char[1024];
                while ((len = inputStreamReader.read(cache)) != -1) {
                    builder.append(new String(cache, 0, len));
                }
                jrXml = builder.toString();
            }
            String subJrXml;
            //noinspection DataFlowIssue
            try (InputStream inputStream = ProjectViewFindDependencyJavaVersionAction.class.getClassLoader()
                    .getResourceAsStream("report/IcyToolsetAnalyzeDependenciesReportTable.jrxml");
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);) {
                StringBuilder builder = new StringBuilder();
                int len;
                char[] cache = new char[1024];
                while ((len = inputStreamReader.read(cache)) != -1) {
                    builder.append(new String(cache, 0, len));
                }
                subJrXml = builder.toString();
            }
            List<PdfGenerateUtil.Request.SubReport> subReports = new ArrayList<>();
            PdfGenerateUtil.Request.SubReport subReport1 = new PdfGenerateUtil.Request.SubReport(0, "subReport1", subJrXml);
            subReports.add(subReport1);
            // 主报表数据源
            List<Map<String, Object>> datasource = new ArrayList<>();
            for (DependencyCheckResult.Dependency dependency : result.getDependencies()) {
                // TODO need menus
                Map<String, Object> eachEntry = new HashMap<>();
                datasource.add(eachEntry);
                Map<String, Object> params = new HashMap<>();
                params.put("dependencyTitle", MessageUtil.getMessage("util.report.template.dependency.title", dependency.getUrl()));
                if (dependency.isSkip) {
                    params.put("dependencyErrorTip", MessageUtil.getMessage("util.report.template.dependency.skip"));
                } else if (dependency.getReadError() != null) {
                    params.put("dependencyErrorTip", dependency.getReadError());
                } else {
                    params.put("dependencyErrorTip", "");
                }
                params.put("dependencyColFilePath", MessageUtil.getMessage("util.report.template.dependency.column.file.path"));
                params.put("dependencyColMajor", MessageUtil.getMessage("util.report.template.dependency.column.major.version"));
                params.put("dependencyColSupportStatus", MessageUtil.getMessage("util.report.template.dependency.column.support.status"));
                List<Map<String, Object>> files = new ArrayList<>();
                for (DependencyCheckResult.CheckedFile file : dependency.getNotSupportFiles()) {
                    Map<String, Object> eachFile = new HashMap<>();
                    eachFile.put("dependencyRowFilePath", file.getFilePath());
                    eachFile.put("dependencyRowMajor", String.valueOf(file.getFileMajor()));
                    eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage(file.isGuess() ? "util.report.template.dependency.row.status.not.support.guessed" : "util.report.template.dependency.row.status.not.support"));
                    eachFile.put("dependencyRowStatusColor", "red");
                    files.add(eachFile);
                }
                for (String file : dependency.getClassNotBeReadFiles()) {
                    Map<String, Object> eachFile = new HashMap<>();
                    eachFile.put("dependencyRowFilePath", file);
                    eachFile.put("dependencyRowMajor", "");
                    eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage("util.report.template.dependency.row.status.unknown"));
                    eachFile.put("dependencyRowStatusColor", "blue");
                    files.add(eachFile);
                }
                // TODO too much
                for (DependencyCheckResult.CheckedFile file : dependency.getSupportFiles()) {
                    Map<String, Object> eachFile = new HashMap<>();
                    eachFile.put("dependencyRowFilePath", file.getFilePath());
                    eachFile.put("dependencyRowMajor", String.valueOf(file.getFileMajor()));
                    eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage(file.isGuess() ? "util.report.template.dependency.row.status.support.guessed" : "util.report.template.dependency.row.status.support"));
                    eachFile.put("dependencyRowStatusColor", "green"); // TODO too light
                    files.add(eachFile);
                }
                Map<String, Object> subMap = new HashMap<>();
                subMap.put("param1", params);
                subMap.put("data1", files);
                eachEntry.put("subMap", subMap);
            }
            // 主报表参数
            Map<String, Object> params = new HashMap<>();
            params.put("reportTitle", MessageUtil.getMessage("util.report.template.title"));
            PdfGenerateUtil.Request request = new PdfGenerateUtil.Request();
            request.setJrXml(jrXml);
            request.setSubReport(subReports);
            request.setParams(params);
            request.setDataSource(datasource);
            request.setExportType(PdfGenerateUtil.Request.ExportType.PDF);
            return PdfGenerateUtil.generate(request);
        } catch (IOException e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.load.jrxml.resource"), e);
        }
    }

    @Data
    private static class DependencyCheckResult {
        private List<Dependency> dependencies = new ArrayList<>();
        @Data
        static class Dependency {
            private String url = null;
            private boolean isSkip = false;
            /**
             * load error message
             */
            private String readError = null;
            /**
             * Module Name, sdk version
             */
            private Map<String, Integer> modules = new HashMap<>();
            private List<CheckedFile> supportFiles = new ArrayList<>();
            private List<CheckedFile> notSupportFiles = new ArrayList<>();
            /**
             * File path
             */
            private List<String> classNotBeReadFiles = new ArrayList<>();
        }
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckedFile {
            private String filePath;
            private int fileMajor;
            private boolean isGuess;
        }
    }

}
