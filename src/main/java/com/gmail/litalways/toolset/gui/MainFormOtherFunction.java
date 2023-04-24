package com.gmail.litalways.toolset.gui;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import com.gmail.litalways.toolset.state.MainSettingState;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.gmail.litalways.toolset.util.NotificationUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author IceRain
 * @since 2022/01/29
 */
public class MainFormOtherFunction {

    private final ToolWindowOther component;
    private VirtualFile generateHashToSelect = null;

    public MainFormOtherFunction(ToolWindowOther component) {
        this.component = component;
        this.component.buttonAboutEncoding.addActionListener(e -> this.component.buttonAboutEncoding.setText(System.getProperty("file.encoding")));
        this.component.buttonAboutGenHashRun.addActionListener(this::generateHash);
        this.component.fileAboutGenHashPath.addActionListener(this::selectGenerateHashPath);
        this.component.selectAboutGenHashType.addActionListener(this::autoWriteGenerateHashSuffix);
        this.component.selectAboutGenHashType.setSelectedItem("SHA-1");
        this.component.textAboutGenHashPathFilter.setText(".*");
        this.component.textAboutGenHashFileFilter.setText(".*");
        this.component.buttonAboutGC.addActionListener(this::forceGC);
    }

    private void selectGenerateHashPath(ActionEvent e) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, true, false, true);
        this.generateHashToSelect = FileChooser.chooseFile(descriptor, null, this.generateHashToSelect);
        if (this.generateHashToSelect != null) {
            this.component.fileAboutGenHashPath.setText(this.generateHashToSelect.getPath());
        }
    }

    private void autoWriteGenerateHashSuffix(ActionEvent e) {
        if (this.component.selectAboutGenHashType.getSelectedIndex() != -1) {
            String type = String.valueOf(this.component.selectAboutGenHashType.getSelectedItem());
            if (!"null".equalsIgnoreCase(type) && type.trim().length() != 0) {
                this.component.textAboutGenHashSuffix.setText("." + type.toLowerCase().replace("-", "").replace("_", ""));
            }
        }
    }

    private void generateHash(ActionEvent event) {
        // valid
        if (this.generateHashToSelect == null) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.not.select.file"));
            return;
        }
        File target = new File(this.generateHashToSelect.getPath());
        if (!target.exists() || !target.canRead()) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.file.not.found"));
            return;
        }
        if (this.component.selectAboutGenHashType.getSelectedIndex() == -1) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.no.hash.type"));
            return;
        }
        String type = String.valueOf(this.component.selectAboutGenHashType.getSelectedItem());
        if ("null".equalsIgnoreCase(type) || type.trim().length() == 0) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.no.hash.type"));
            return;
        }
        String suffix = this.component.textAboutGenHashSuffix.getText();
        if ("null".equalsIgnoreCase(suffix) || suffix.trim().length() == 0) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.suffix.can.not.be.none"));
            return;
        }
        String pathPattern = this.component.textAboutGenHashPathFilter.getText();
        if ("null".equalsIgnoreCase(pathPattern) || pathPattern.trim().length() == 0) {
            this.component.textAboutGenHashPathFilter.setText(".*");
            pathPattern = ".*";
        }
        String filenamePattern = this.component.textAboutGenHashFileFilter.getText();
        if ("null".equalsIgnoreCase(filenamePattern) || filenamePattern.trim().length() == 0) {
            this.component.textAboutGenHashFileFilter.setText(".*");
            filenamePattern = ".*";
        }
        boolean genPom = this.component.checkAboutGenHashPom.isSelected();
        // run
        List<File> files = new ArrayList<>();
        if (target.isDirectory()) {
            Pattern regexPath;
            Pattern regexFilename;
            try {
                regexPath = Pattern.compile(pathPattern);
                regexFilename = Pattern.compile(filenamePattern);
            } catch (Exception e) {
                NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.path.or.filename.regex.not.valid"));
                return;
            }
            getFilesInDir(target, regexPath, regexFilename, files);
        } else if (target.isFile()) {
            files.add(target);
        }
        for (File f : files) {
            boolean isDependence = f.getName().endsWith(".jar") && !(f.getName().endsWith("javadoc.jar") || f.getName().endsWith("sources.jar") || f.getName().endsWith("src.jar"));
            if (genPom && isDependence) {
                String pomStr = null;
                String jarName = f.getName();
                String artifactId = null;
                String groupId = null;
                Map<String, String> pom = new HashMap<>();
                try (FileInputStream fis = new FileInputStream(f);
                     ZipInputStream zip = new ZipInputStream(fis)) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.startsWith("META-INF") && name.endsWith("pom.properties")) {
                            Properties properties = new Properties();
                            properties.load(zip);
                            String _artifactId = properties.getProperty("artifactId");
                            String _groupId = properties.getProperty("groupId");
                            if (jarName.contains(_artifactId)) {
                                artifactId = _artifactId;
                                groupId = _groupId;
                            }
                        } else if (name.startsWith("META-INF") && name.endsWith("pom.xml")) {
                            StringBuilder stringBuilder = new StringBuilder();
                            int n;
                            while ((n = zip.read()) != -1) {
                                stringBuilder.append((char) n);
                            }
                            pom.put(name, stringBuilder.toString());
                        }
                    }
                } catch (Exception e) {
                    NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.unzip.pom.error", f.getName(), e.getClass().getSimpleName(), e.getLocalizedMessage()));
                }
                if (artifactId != null && groupId != null && pom.size() > 0) {
                    for (Map.Entry<String, String> entry : pom.entrySet()) {
                        if (entry.getKey().contains(artifactId) && entry.getKey().contains(groupId)) {
                            pomStr = entry.getValue();
                            break;
                        }
                    }
                }
                if (pomStr != null && pomStr.trim().length() > 0) {
                    boolean writeSuccess = false;
                    File pomFile = new File(f.getPath().substring(0, f.getPath().length() - f.getName().length()), f.getName().substring(0, f.getName().length() - 3) + "pom");
                    try (FileWriter fw = new FileWriter(pomFile, StandardCharsets.UTF_8)) {
                        fw.write(pomStr);
                        writeSuccess = true;
                    } catch (Exception e) {
                        NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.write.pom.error", f.getName(), e.getClass().getSimpleName(), e.getLocalizedMessage()));
                    }
                    if (writeSuccess) {
                        hashFileAndWriteHashFile(pomFile, type, suffix);
                    }
                } else {
                    NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.unzip.pom.error", f.getName(), "", ""));
                }
            }
            hashFileAndWriteHashFile(f, type, suffix);
        }
        NotificationUtil.info(MessageUtil.getMessage("other.generate.hash.tip.done"));
    }

    private static void getFilesInDir(File dir, Pattern patternPath, Pattern patternFilename, List<File> back) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    getFilesInDir(f, patternPath, patternFilename, back);
                }
            }
        } else if (dir.isFile()) {
            boolean matchesPath = patternPath.matcher(dir.getPath().substring(0, dir.getPath().length() - dir.getName().length())).matches();
            boolean matchesFilename = patternFilename.matcher(dir.getName()).matches();
            if (matchesPath && matchesFilename) {
                back.add(dir);
            }
        }
    }

    private static void hashFileAndWriteHashFile(File src, String hashType, String fileSuffixWithDot) {
        File target = new File(src.getPath().substring(0, src.getPath().length() - src.getName().length()), src.getName() + fileSuffixWithDot);
        try (FileWriter fw = new FileWriter(target, StandardCharsets.UTF_8)) {
            Digester digester = DigestUtil.digester(hashType);
            String resultHex = digester.digestHex(src);
            fw.write(resultHex);
        } catch (Exception e) {
            NotificationUtil.error(MessageUtil.getMessage("other.generate.hash.tip.write.hash.error", src.getName(), e.getClass().getSimpleName(), e.getLocalizedMessage()));
        }
    }

    private void forceGC(ActionEvent event) {
        System.gc();
    }

}
