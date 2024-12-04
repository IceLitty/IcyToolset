package com.gmail.litalways.toolset.action;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.exception.ProxyException;
import com.gmail.litalways.toolset.exception.UserCancelActionException;
import com.gmail.litalways.toolset.filter.PdfFileFilter;
import com.gmail.litalways.toolset.util.*;
import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 批量检测项目依赖所需java版本（如用于java8项目检测是否依赖需要java9及以上）
 *
 * @author IceRain
 * @since 2024/8/1
 */
@Slf4j
public class ProjectViewFindDependencyJavaVersionAction extends AnAction {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("messages/message");
    private static File lastSelect = null;

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
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.dependency.java.version.searching")) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // BGT
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT with read access
                    try {
                        Module[] modules = ModuleManager.getInstance(project).getModules();
                        AtomicInteger pgInFinished = new AtomicInteger(0);
                        AtomicInteger pgInMax = new AtomicInteger(modules.length);
                        DependencyCheckResult result = new DependencyCheckResult();
                        for (Module module : modules) {
                            if (progressIndicator.isCanceled()) {
                                throw new UserCancelActionException();
                            }
                            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                            Sdk sdk = moduleRootManager.getSdk();
                            JavaSdkVersion jdkVersion;
                            if (sdk != null && sdk.getSdkType() instanceof JavaSdk jdk && (jdkVersion = jdk.getVersion(sdk)) != null) {
                                int feature = jdkVersion.getMaxLanguageLevel().toJavaVersion().feature;
                                VirtualFile[] classesRoots = moduleRootManager.orderEntries().librariesOnly().getClassesRoots();
                                pgInMax.addAndGet(classesRoots.length);
                                for (VirtualFile dependency : classesRoots) {
                                    if (progressIndicator.isCanceled()) {
                                        throw new UserCancelActionException();
                                    }
                                    try {
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
                                                resultDependency.setReadError(MessageUtil.getMessage("action.find.dependency.java.version.file.system.error", ex.getLocalizedMessage()));
                                            }
                                        } else if (dependency.getFileType() instanceof JavaClassFileType) {
                                            try (InputStream inputStream = dependency.getInputStream();
                                                 DataInputStream dataInputStream = new DataInputStream(inputStream);) {
                                                checkClassFile(feature, dependency.getPath(), dataInputStream, resultDependency);
                                            } catch (IOException ex) {
                                                resultDependency.setReadError(MessageUtil.getMessage("action.find.dependency.java.version.file.system.error", ex.getLocalizedMessage()));
                                            }
                                        } else {
                                            resultDependency.setSkip(true);
                                        }
                                    } finally {
                                        progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                                    }
                                }
                            }
                            progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
                        }
                        // output
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            int sum = result.getDependencies().stream()
                                    .map(DependencyCheckResult.Dependency::getCheckedFiles)
                                    .flatMap(Collection::stream)
                                    .mapToInt(d -> switch (d.getResult()) {
                                        case SUPPORT, SUPPORT_GUESS, SKIP_CHECK -> 0;
                                        case UNKNOWN, NOT_SUPPORT_GUESS, NOT_SUPPORT -> 1;
                                    }).sum();
                            NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup(KeyConstant.NOTIFICATION_GROUP_KEY);
                            if (sum == 0) {
                                group.createNotification(MessageUtil.getMessage("action.find.dependency.java.version.done", modules.length, result.getDependencies().size(), sum), NotificationType.INFORMATION)
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.clipboard.full.text"), project, result, BalloonActionType.TO_CLIPBOARD_FULL))
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.file.full.text"), project, result, BalloonActionType.TO_FILE_FULL))
                                        .notify(project);
                            } else {
                                group.createNotification(MessageUtil.getMessage("action.find.dependency.java.version.done", modules.length, result.getDependencies().size(), sum), NotificationType.INFORMATION)
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.clipboard.text"), project, result, BalloonActionType.TO_CLIPBOARD))
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.file.text"), project, result, BalloonActionType.TO_FILE))
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.clipboard.full.text"), project, result, BalloonActionType.TO_CLIPBOARD_FULL))
                                        .addAction(new BalloonAction(messageBundle.getString("action.com.gmail.litalways.toolset.action.ProjectViewFindDependencyJavaVersionAction.BalloonAction.to.file.full.text"), project, result, BalloonActionType.TO_FILE_FULL))
                                        .notify(project);
                            }
                        });
                    } catch (UserCancelActionException e) {
                        NotificationUtil.warning(MessageUtil.getMessage("action.cancel"));
                    }
                });
            }
        });
    }

    private void checkClassFile(int moduleJdkVersion, String fileName, DataInputStream dataInputStream, DependencyCheckResult.Dependency resultDependency) throws IOException {
        DependencyCheckResult.CheckedFile checkedFile = new DependencyCheckResult.CheckedFile();
        checkedFile.setFilePath(fileName);
        int magic = dataInputStream.readInt();
        if (magic != 0xCAFEBABE) {
            checkedFile.setResult(DependencyCheckResult.CheckedResult.UNKNOWN);
            resultDependency.getCheckedFiles().add(checkedFile);
            return;
        }
        //noinspection unused
        int minor = dataInputStream.readUnsignedShort();
        int major = dataInputStream.readUnsignedShort();
        checkedFile.setFileMajor(major);
        String[] fileNamesCheck = fileName.split("/");
        if (fileNamesCheck.length > 0) {
            if ("module-info.class".equals(fileNamesCheck[fileNamesCheck.length - 1]) || "package-info.class".equals(fileNamesCheck[fileNamesCheck.length - 1])) {
                checkedFile.setResult(DependencyCheckResult.CheckedResult.SKIP_CHECK);
                resultDependency.getCheckedFiles().add(checkedFile);
                return;
            }
        }
        if (fileNamesCheck.length > 1) {
            if ("META-INF".equals(fileNamesCheck[1])) {
                checkedFile.setResult(DependencyCheckResult.CheckedResult.SKIP_CHECK);
                resultDependency.getCheckedFiles().add(checkedFile);
                return;
            }
        }
        Boolean capability = ClassVersionUtil.checkCapability(moduleJdkVersion, major);
        boolean guess = false;
        if (capability == null) {
            guess = true;
            capability = ClassVersionUtil.guessCapability(moduleJdkVersion, major);
        }
        if (capability) {
            checkedFile.setResult(guess ? DependencyCheckResult.CheckedResult.SUPPORT_GUESS : DependencyCheckResult.CheckedResult.SUPPORT);
        } else {
            checkedFile.setResult(guess ? DependencyCheckResult.CheckedResult.NOT_SUPPORT_GUESS : DependencyCheckResult.CheckedResult.NOT_SUPPORT);
        }
        resultDependency.getCheckedFiles().add(checkedFile);
    }

    private static String generatePdf(DependencyCheckResult result, boolean outputMore, ProgressIndicator progressIndicator, AtomicInteger pgInFinished, AtomicInteger pgInMax) throws ProxyException, UserCancelActionException {
        try {
            Map<String, Integer> bookmark = new LinkedHashMap<>();
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
            pgInMax.addAndGet(result.getDependencies().size());
            for (int i = 0, dependenciesSize = result.getDependencies().size(); i < dependenciesSize; i++) {
                if (progressIndicator.isCanceled()) {
                    throw new UserCancelActionException();
                }
                DependencyCheckResult.Dependency dependency = result.getDependencies().get(i);
                AtomicInteger supportCounter = new AtomicInteger(0);
                AtomicInteger unknownCounter = new AtomicInteger(0);
                AtomicInteger skipCounter = new AtomicInteger(0);
                AtomicInteger notSupportCounter = new AtomicInteger(0);
                List<Map<String, Object>> files = new ArrayList<>();
                dependency.getCheckedFiles().stream()
                        .filter(d -> d.getResult() == DependencyCheckResult.CheckedResult.NOT_SUPPORT || d.getResult() == DependencyCheckResult.CheckedResult.NOT_SUPPORT_GUESS).forEach(file -> {
                            Map<String, Object> eachFile = new HashMap<>();
                            eachFile.put("dependencyRowFilePath", file.getFilePath());
                            eachFile.put("dependencyRowMajor", String.valueOf(file.getFileMajor()));
                            eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage(file.getResult() == DependencyCheckResult.CheckedResult.NOT_SUPPORT_GUESS ? "util.report.template.dependency.row.status.not.support.guessed" : "util.report.template.dependency.row.status.not.support"));
                            eachFile.put("dependencyRowStatusColor", "red");
                            files.add(eachFile);
                            notSupportCounter.incrementAndGet();
                        });
                dependency.getCheckedFiles().stream()
                        .filter(d -> d.getResult() == DependencyCheckResult.CheckedResult.UNKNOWN).forEach(file -> {
                            Map<String, Object> eachFile = new HashMap<>();
                            eachFile.put("dependencyRowFilePath", file.getFilePath());
                            eachFile.put("dependencyRowMajor", "");
                            eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage("util.report.template.dependency.row.status.unknown"));
                            eachFile.put("dependencyRowStatusColor", "blue");
                            files.add(eachFile);
                            unknownCounter.incrementAndGet();
                        });
                if (outputMore) {
                    dependency.getCheckedFiles().stream()
                            .filter(d -> d.getResult() == DependencyCheckResult.CheckedResult.SKIP_CHECK).forEach(file -> {
                                Map<String, Object> eachFile = new HashMap<>();
                                eachFile.put("dependencyRowFilePath", file.getFilePath());
                                eachFile.put("dependencyRowMajor", "");
                                eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage("util.report.template.dependency.row.status.skip"));
                                eachFile.put("dependencyRowStatusColor", "gray");
                                files.add(eachFile);
                                skipCounter.incrementAndGet();
                            });
                    dependency.getCheckedFiles().stream()
                            .filter(d -> d.getResult() == DependencyCheckResult.CheckedResult.SUPPORT || d.getResult() == DependencyCheckResult.CheckedResult.SUPPORT_GUESS).forEach(file -> {
                                Map<String, Object> eachFile = new HashMap<>();
                                eachFile.put("dependencyRowFilePath", file.getFilePath());
                                eachFile.put("dependencyRowMajor", String.valueOf(file.getFileMajor()));
                                eachFile.put("dependencyRowSupportStatus", MessageUtil.getMessage(file.getResult() == DependencyCheckResult.CheckedResult.SUPPORT_GUESS ? "util.report.template.dependency.row.status.support.guessed" : "util.report.template.dependency.row.status.support"));
                                eachFile.put("dependencyRowStatusColor", "#6AAB73");
                                files.add(eachFile);
                                supportCounter.incrementAndGet();
                            });
                } else {
                    skipCounter.set(dependency.getCheckedFiles().stream().mapToInt(d -> d.getResult() == DependencyCheckResult.CheckedResult.SKIP_CHECK ? 1 : 0).sum());
                    supportCounter.set(dependency.getCheckedFiles().stream().mapToInt(d -> (d.getResult() == DependencyCheckResult.CheckedResult.SUPPORT || d.getResult() == DependencyCheckResult.CheckedResult.SUPPORT_GUESS) ? 1 : 0).sum());
                }
                Map<String, Object> params = new HashMap<>();
                if (dependency.isSkip) {
                    params.put("dependencyErrorTip", MessageUtil.getMessage("util.report.template.dependency.skip"));
                } else if (dependency.getReadError() != null) {
                    params.put("dependencyErrorTip", dependency.getReadError());
                } else {
                    params.put("dependencyErrorTip", "");
                }
                params.put("dependencyTitle", MessageUtil.getMessage("util.report.template.dependency.title", i + 1, dependency.getUrl(), notSupportCounter.get(), unknownCounter.get(), skipCounter.get(), supportCounter.get()));
                params.put("dependencyColFilePath", MessageUtil.getMessage("util.report.template.dependency.column.file.path"));
                params.put("dependencyColMajor", MessageUtil.getMessage("util.report.template.dependency.column.major.version"));
                params.put("dependencyColSupportStatus", MessageUtil.getMessage("util.report.template.dependency.column.support.status"));
                Map<String, Object> subMap = new HashMap<>();
                subMap.put("param1", params);
                subMap.put("data1", files);
                // 当选择省略输出模式，忽略掉无条目的依赖输出
                if (outputMore || !files.isEmpty()) {
                    bookmark.put((String) params.get("dependencyTitle"), 1);
                    Map<String, Object> eachEntry = new HashMap<>();
                    eachEntry.put("subMap", subMap);
                    datasource.add(eachEntry);
                }
                progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
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
            String base64 = PdfGenerateUtil.generate(request);
            byte[] pdfBytes = Base64.getDecoder().decode(base64);
            // 生成书签
            PdfReader reader = new PdfReader(pdfBytes);
            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
            Map<Integer, String> textFromPdf = new HashMap<>();
            int max = reader.getNumberOfPages();
            pgInMax.addAndGet(max);
            for (int i = 1; i <= max; i++) {
                if (progressIndicator.isCanceled()) {
                    throw new UserCancelActionException();
                }
                String textFromPage = pdfTextExtractor.getTextFromPage(i);
                textFromPdf.put(i, textFromPage);
                progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
            }
            boolean foundFailed = false;
            String notFoundBookmark = null;
            for (Map.Entry<String, Integer> m : bookmark.entrySet()) {
                String text = m.getKey();
                boolean found = false;
                for (Map.Entry<Integer, String> pageEntry : textFromPdf.entrySet()) {
                    Integer no = pageEntry.getKey();
                    String allText = pageEntry.getValue();
                    if (allText != null && allText.replace("\n", "").contains(text)) {
                        m.setValue(no);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    foundFailed = true;
                    notFoundBookmark = text;
                    break;
                }
            }
            pgInMax.addAndGet(1);
            if (progressIndicator.isCanceled()) {
                throw new UserCancelActionException();
            }
            try {
                pdfBytes = PdfGenerateUtil.bookmark(pdfBytes, bookmark);
                base64 = Base64.getEncoder().encodeToString(pdfBytes);
                if (foundFailed) {
                    NotificationUtil.error(MessageUtil.getMessage("util.report.generator.bookmark.fail", notFoundBookmark));
                }
            } catch (DocumentException e) {
                log.error(MessageUtil.getMessage("util.report.generator.bookmark.fail", ""), e);
                NotificationUtil.error(MessageUtil.getMessage("util.report.generator.bookmark.fail", ""), e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
            progressIndicator.setFraction((double) pgInFinished.incrementAndGet() / pgInMax.get());
            return base64;
        } catch (IOException e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.load.jrxml.resource"), e);
        }
    }

    @Slf4j
    private static class BalloonAction extends AnAction {
        private final Project project;
        private final DependencyCheckResult result;
        private final BalloonActionType type;
        public BalloonAction(@Nullable @NlsActions.ActionText String text, Project project, DependencyCheckResult result, BalloonActionType type) {
            super(text);
            this.project = project;
            this.result = result;
            this.type = type;
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageUtil.getMessage("action.find.dependency.java.version.generating")) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    // BGT
                    try {
                        AtomicInteger pgInFinished = new AtomicInteger(0);
                        AtomicInteger pgInMax = new AtomicInteger(0);
                        String base64 = generatePdf(result, type == BalloonActionType.TO_CLIPBOARD_FULL || type == BalloonActionType.TO_FILE_FULL, progressIndicator, pgInFinished, pgInMax);
                        byte[] decode = Base64.getDecoder().decode(base64);
                        ApplicationManager.getApplication().invokeLater(() -> {
                            // EDT
                            if (type == BalloonActionType.TO_CLIPBOARD || type == BalloonActionType.TO_CLIPBOARD_FULL) {
                                CopyPasteManager.getInstance().setContents(new FileTransferable(new FileTransferable.File("output", "pdf", decode)));
                                NotificationUtil.info(MessageUtil.getMessage("action.find.dependency.java.version.clipboard.done"));
                            } else {
                                JFileChooser fileChooser = new JFileChooser(lastSelect);
                                fileChooser.setFileFilter(new PdfFileFilter());
                                int status = fileChooser.showSaveDialog(null);
                                if (status == JFileChooser.APPROVE_OPTION) {
                                    try {
                                        File file = fileChooser.getSelectedFile();
                                        if (!file.getName().toLowerCase().endsWith(".pdf")) {
                                            file = new File(file.getParentFile(), file.getName() + ".pdf");
                                        }
                                        if (file.exists()) {
                                            //noinspection ResultOfMethodCallIgnored
                                            file.delete();
                                        }
                                        lastSelect = file.getParentFile();
                                        Files.write(file.toPath(), decode, StandardOpenOption.CREATE_NEW);
                                        ExplorerUtil.openExplorerAndHighlightFile(file);
                                    } catch (Exception ex) {
                                        NotificationUtil.error(ex.getClass().getName(), ex.getLocalizedMessage());
                                    }
                                }
                            }
                        });
                    } catch (UserCancelActionException e) {
                        NotificationUtil.warning(MessageUtil.getMessage("action.cancel"));
                    } catch (ProxyException e) {
                        NotificationUtil.error(e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private enum BalloonActionType {
        TO_CLIPBOARD,
        TO_FILE,
        TO_CLIPBOARD_FULL,
        TO_FILE_FULL,
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
            private List<CheckedFile> checkedFiles = new ArrayList<>();
        }
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckedFile {
            private String filePath;
            private int fileMajor;
            private CheckedResult result;
        }
        enum CheckedResult {
            SUPPORT,
            SUPPORT_GUESS,
            UNKNOWN,
            NOT_SUPPORT,
            NOT_SUPPORT_GUESS,
            SKIP_CHECK,
        }
    }

}
