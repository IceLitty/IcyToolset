package com.gmail.litalways.toolset.gui;

import cn.hutool.http.HttpUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.RegisteredClaims;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.gmail.litalways.toolset.filter.LongNumberTextFormat;
import com.gmail.litalways.toolset.service.ToolWindowEncryptEditorService;
import com.gmail.litalways.toolset.util.JWKUtil;
import com.gmail.litalways.toolset.util.JWTBiPredicate;
import com.gmail.litalways.toolset.util.JsonUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.List;

/**
 * @author IceRain
 * @since 2023/04/07
 */
public class ToolWindowEncrypt {

    JPanel panelMain;
    JTabbedPane tabEncrypt;
    JComboBox<String> selectEncryptHashEncoding;
    JComboBox<String> selectEncryptHashType;
    JButton buttonEncryptHashFile;
    JButton buttonEncryptHashText;
    JCheckBox checkEncryptHashLine;
    JComboBox<String> selectEncryptHashOutputType;
    JButton buttonEncryptHashClean;
    JScrollPane scrollEncryptHashText;
    JTextArea textareaEncryptHashText;
    TextFieldWithBrowseButton fileEncryptHashFile;
    JButton buttonEncryptHashOpenDirectory;
    JTextField textEncryptHashKey;
    JButton buttonEncryptHashGenerateKey;
    JScrollPane scrollEncryptHashResult;
    JTextArea textareaEncryptHashResult;
    JTextField textEncryptHashAssert;
    TextFieldWithBrowseButton fileEncryptHashAsserts;
    JButton buttonEncryptHashAssertsOpenDirectory;
    JTextField textEncryptAsymmetricPublicKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPublicKey;
    JTextField textEncryptAsymmetricPrivateKey;
    TextFieldWithBrowseButton fileEncryptAsymmetricPrivateKey;
    JButton buttonEncryptAsymmetricEncryptWithPublicKey;
    JButton buttonEncryptAsymmetricDecryptWithPrivateKey;
    JButton buttonEncryptAsymmetricEncryptWithPrivateKey;
    JButton buttonEncryptAsymmetricDecryptWithPublicKey;
    JComboBox<String> selectEncryptAsymmetricEncoding;
    JComboBox<String> selectEncryptAsymmetricType;
    JButton buttonEncryptAsymmetricGenerateKey;
    JButton buttonEncryptAsymmetricClean;
    JScrollPane scrollEncryptAsymmetricEncrypted;
    JTextArea textareaEncryptAsymmetricEncrypted;
    JScrollPane scrollEncryptAsymmetricDecrypted;
    JTextArea textareaEncryptAsymmetricDecrypted;
    JComboBox<String> selectEncryptSymmetricType;
    JComboBox<String> selectEncryptSymmetricMode;
    JComboBox<String> selectEncryptSymmetricPadding;
    JComboBox<String> selectEncryptSymmetricOutputType;
    JTextField textEncryptSymmetricKey;
    JTextField textEncryptSymmetricIV;
    JTextField textEncryptSymmetricSalt;
    JButton buttonEncryptSymmetricEncrypt;
    JButton buttonEncryptSymmetricDecrypt;
    JComboBox<String> selectEncryptSymmetricEncoding;
    JButton buttonEncryptSymmetricClean;
    JScrollPane scrollEncryptSymmetricDecrypted;
    JTextArea textareaEncryptSymmetricDecrypted;
    JScrollPane scrollEncryptSymmetricEncrypted;
    JTextArea textareaEncryptSymmetricEncrypted;
    JLabel jwtVerifyStatusIcon;
    JLabel jwtVerifyStatus;
    JComponent jwtPublicKeyEditor;
    JComponent jwtPrivateKeyEditor;
    JComponent jwtDecodedEditor;
    JComponent jwtEncodedEditor;
    EditorEx jwtPublicKeyEditorEx;
    EditorEx jwtPrivateKeyEditorEx;
    EditorEx jwtDecodedEditorEx;
    EditorEx jwtEncodedEditorEx;
    ADocumentListener jwtPublicKeyDocumentListener;
    ADocumentListener jwtPrivateKeyDocumentListener;
    ADocumentListener jwtDecodedDocumentListener;
    ADocumentListener jwtEncodedDocumentListener;
    JButton jwtEncodeBtn;
    JButton jwtDecodeBtn;
    JCheckBox jwtAutoRun;
    JComboBox<String> jwtSelectAlgorithm;
    JFormattedTextField jwtIssuedAt;
    JFormattedTextField jwtNotBefore;
    JFormattedTextField jwtExpirationTime;
    JTextField jwtAudience;
    JTextField jwtIssuer;
    JTextField jwtSubject;
    JTextField jwtID;
    JButton jwtCleanBtn;
    JButton jwtPemJwkSwitchBtn;

    private final Project project;
    private final ToolWindow toolWindow;

    /**
     * TRUE: encode
     * FALSE: decode
     */
    private boolean lastFunc;
    /**
     * TRUE: 阻止自动执行
     * FALSE: 恢复自动执行
     */
    boolean stateLock;
    private final List<String> lostKey;

    public ToolWindowEncrypt(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.lastFunc = true;
        this.stateLock = false;
        this.lostKey = new ArrayList<>();
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
        this.jwtNotBefore = new JFormattedTextField(LongNumberTextFormat.getInstance());
        this.jwtIssuedAt = new JFormattedTextField(LongNumberTextFormat.getInstance());
        this.jwtExpirationTime = new JFormattedTextField(LongNumberTextFormat.getInstance());
        ToolWindowEncryptEditorService editorService = this.project.getService(ToolWindowEncryptEditorService.class);
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtEncoded.txt", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtEncodedEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtEncodedEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtEncodedDocumentListener = new ADocumentListener(this.jwtEncodedEditorEx, this);
            this.jwtEncodedEditorEx.getDocument().addDocumentListener(this.jwtEncodedDocumentListener);
            this.jwtEncodedEditor = this.jwtEncodedEditorEx.getComponent();
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtDecoded.json", JsonFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtDecodedEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtDecodedEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtDecodedDocumentListener = new ADocumentListener(this.jwtDecodedEditorEx, this);
            this.jwtDecodedEditorEx.getDocument().addDocumentListener(this.jwtDecodedDocumentListener);
            this.jwtDecodedEditor = this.jwtDecodedEditorEx.getComponent();
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtJwkPublicKey.json", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtPublicKeyEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtPublicKeyEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtPublicKeyDocumentListener = new ADocumentListener(this.jwtPublicKeyEditorEx, this);
            this.jwtPublicKeyEditorEx.getDocument().addDocumentListener(this.jwtPublicKeyDocumentListener);
            this.jwtPublicKeyEditor = this.jwtPublicKeyEditorEx.getComponent();
            this.jwtPublicKeyEditor.setToolTipText(MessageUtil.getMessage("encrypt.jwt.editor.public.tooltip"));
        }
        {
            PsiFile file = PsiFileFactory.getInstance(this.project).createFileFromText("jwtJwkPrivateKey.json", PlainTextFileType.INSTANCE, "", 0, true);
            Document document = PsiDocumentManager.getInstance(this.project).getDocument(file);
            if (document == null) {
                document = EditorFactory.getInstance().createDocument("");
            }
            this.jwtPrivateKeyEditorEx = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project);
            this.jwtPrivateKeyEditorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(this.project, file.getVirtualFile()));
            this.jwtPrivateKeyDocumentListener = new ADocumentListener(this.jwtPrivateKeyEditorEx, this);
            this.jwtPrivateKeyEditorEx.getDocument().addDocumentListener(this.jwtPrivateKeyDocumentListener);
            this.jwtPrivateKeyEditor = this.jwtPrivateKeyEditorEx.getComponent();
            this.jwtPrivateKeyEditor.setToolTipText(MessageUtil.getMessage("encrypt.jwt.editor.private.tooltip"));
        }
        editorService.setEditors(this.jwtEncodedEditorEx, this.jwtDecodedEditorEx, this.jwtPublicKeyEditorEx, this.jwtPrivateKeyEditorEx);
    }

    public void encode() {
        lastFunc = true;
        this.jwtVerifyStatus.setText("");
        JWTCreator.Builder builder = JWT.create();
        if (this.jwtIssuer.getText() != null && !this.jwtIssuer.getText().isEmpty()) {
            builder.withIssuer(this.jwtIssuer.getText());
        }
        if (this.jwtSubject.getText() != null && !this.jwtSubject.getText().isEmpty()) {
            builder.withSubject(this.jwtSubject.getText());
        }
        if (this.jwtAudience.getText() != null && !this.jwtAudience.getText().isEmpty()) {
            boolean jsonSuc = false;
            String audienceText = this.jwtAudience.getText();
            if (audienceText.startsWith("[")) {
                try {
                    List<String> list = JsonUtil.INSTANCE.readValue(audienceText, new TypeReference<List<String>>() {
                    });
                    jsonSuc = true;
                    builder.withAudience(list.toArray(new String[0]));
                } catch (JsonProcessingException e) {}
            }
            if (!jsonSuc) {
                builder.withAudience(this.jwtAudience.getText());
            }
        }
        if (this.jwtID.getText() != null && !this.jwtID.getText().isEmpty()) {
            builder.withJWTId(this.jwtID.getText());
        }
        if (this.jwtNotBefore.getText() != null && !this.jwtNotBefore.getText().isEmpty()) {
            try {
                builder.withNotBefore(new Date(Long.parseLong(this.jwtNotBefore.getText() + "000")));
            } catch (NumberFormatException e) {
                this.jwtNotBefore.setText("");
                return;
            }
        }
        if (this.jwtIssuedAt.getText() != null && !this.jwtIssuedAt.getText().isEmpty()) {
            try {
                builder.withIssuedAt(new Date(Long.parseLong(this.jwtIssuedAt.getText() + "000")));
            } catch (NumberFormatException e) {
                this.jwtIssuedAt.setText("");
                return;
            }
        }
        if (this.jwtExpirationTime.getText() != null && !this.jwtExpirationTime.getText().isEmpty()) {
            try {
                builder.withExpiresAt(new Date(Long.parseLong(this.jwtExpirationTime.getText() + "000")));
            } catch (NumberFormatException e) {
                this.jwtExpirationTime.setText("");
                return;
            }
        }
        String customClaims = this.jwtDecodedEditorEx.getDocument().getText();
        if (!customClaims.isEmpty()) {
            lostKey.clear();
            try {
                Map<String, Object> claims = JsonUtil.INSTANCE.readValue(customClaims, new TypeReference<Map<String, Object>>() {
                });
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        builder.withClaim(entry.getKey(), (Boolean) entry.getValue());
                    } else if (entry.getValue() instanceof Integer) {
                        builder.withClaim(entry.getKey(), (Integer) entry.getValue());
                    } else if (entry.getValue() instanceof Long) {
                        builder.withClaim(entry.getKey(), (Long) entry.getValue());
                    } else if (entry.getValue() instanceof Double) {
                        builder.withClaim(entry.getKey(), (Double) entry.getValue());
                    } else if (entry.getValue() instanceof String) {
                        builder.withClaim(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Map) {
                        builder.withClaim(entry.getKey(), (Map) entry.getValue());
                    } else if (entry.getValue() instanceof List) {
                        builder.withClaim(entry.getKey(), (List) entry.getValue());
                    } else {
                        lostKey.add(entry.getKey());
                    }
                }
            } catch (JsonProcessingException e) {
                this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.wrong.claims.json.string"));
            } catch (Exception e) {
                this.jwtVerifyStatus.setText(e.getLocalizedMessage());
            }
        }
        Algorithm alg = null;
        String selectedItem = (String) this.jwtSelectAlgorithm.getSelectedItem();
        if (selectedItem != null) {
            String priv = this.jwtPrivateKeyEditorEx.getDocument().getText();
            String pub = this.jwtPublicKeyEditorEx.getDocument().getText();
            switch (selectedItem) {
                case "HS256", "HS384", "HS512" -> {
                    if (priv.isBlank() && pub.isBlank()) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", ""));
                        return;
                    }
                }
                default -> {
                    if (priv.isBlank()) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", ""));
                        return;
                    }
                }
            }
            switch (selectedItem) {
                case "HS256" -> alg = Algorithm.HMAC256(priv.isEmpty() ? pub : priv);
                case "HS384" -> alg = Algorithm.HMAC384(priv.isEmpty() ? pub : priv);
                case "HS512" -> alg = Algorithm.HMAC512(priv.isEmpty() ? pub : priv);
                case "RS256", "RS384", "RS512" -> {
                    try {
                        byte[] privkeyEncoded = JWKUtil.pem2byte(priv);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        try {
                            privkeyEncoded = Base64.getDecoder().decode(privkeyEncoded);
                        } catch (IllegalArgumentException ignored) {}
                        RSAPrivateKey rsaPrivKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privkeyEncoded));
                        switch (selectedItem) {
                            case "RS256" -> alg = Algorithm.RSA256(rsaPrivKey);
                            case "RS384" -> alg = Algorithm.RSA384(rsaPrivKey);
                            case "RS512" -> alg = Algorithm.RSA512(rsaPrivKey);
                        }
                    } catch (Exception e) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                        return;
                    }
                }
                case "ES256", "ES384", "ES512" -> {
                    try {
                        byte[] sec1 = Base64.getDecoder().decode(priv);
                        KeyFactory kf = KeyFactory.getInstance("EC");
                        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sec1);
                        ECPrivateKey privateKey = (ECPrivateKey) kf.generatePrivate(keySpec);
                        switch (selectedItem) {
                            case "ES256" -> alg = Algorithm.ECDSA256(privateKey);
                            case "ES384" -> alg = Algorithm.ECDSA384(privateKey);
                            case "ES512" -> alg = Algorithm.ECDSA512(privateKey);
                        }
                    } catch (Exception e) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                        return;
                    }
                }
            }
        }
        if (alg == null) {
            this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.algorithm"));
            return;
        }
        String token = builder.sign(alg);
        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                this.jwtEncodedEditorEx.getDocument().setText(token);
            });
        });
    }

    public void decode() {
        lastFunc = false;
        this.jwtVerifyStatus.setText("");
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(this.jwtEncodedEditorEx.getDocument().getText());
        } catch (JWTDecodeException e) {
            return;
        }
        this.jwtSelectAlgorithm.setSelectedItem(jwt.getAlgorithm());
        this.jwtSubject.setText(jwt.getSubject());
        List<String> audience = jwt.getAudience();
        if (audience != null && !audience.isEmpty()) {
            if (audience.size() == 1) {
                this.jwtAudience.setText(audience.get(0));
            } else {
                try {
                    this.jwtAudience.setText(JsonUtil.INSTANCE.writeValueAsString(audience));
                } catch (JsonProcessingException e) {
                    this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.audience.json.write.error", e.getLocalizedMessage()));
                    return;
                }
            }
        }
        String issuer = jwt.getIssuer();
        if (issuer != null && !issuer.trim().isEmpty()) {
            this.jwtIssuer.setText(issuer);
            final String url = issuer + "/.well-known/openid-configuration";
            // 猜测Issuer是网站域名或地址，如abc.com/issuer
            if (issuer.contains(".")) {
                // 异步网络请求，下载JWK
                ApplicationManager.getApplication().runReadAction(() -> {
                    // BGT
                    String data = null;
                    try {
                        data = HttpUtil.get(url, 60);
                    } catch (Exception e) {
                        ApplicationManager.getApplication().invokeLater(() -> {
//                        ApplicationManager.getApplication().runWriteAction(() -> {
                            // EDT
                            this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.jwk.download.failed.error", url));
                        });
                    }
                    if (data != null) {
                        // 写入公钥JWK窗口
                        final String s;
                        String s1;
                        try {
                            Map<String, Object> test = JsonUtil.INSTANCE.readValue(data, new TypeReference<Map<String, Object>>() {
                            });
                            s1 = data;
                        } catch (JsonProcessingException ignored) {
                            s1 = "";
                        }
                        s = s1;
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            // EDT
                            CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                                this.jwtPublicKeyEditorEx.getDocument().setText(s);
                            });
                        });
                    }
                });
            }
        }
        if (jwt.getExpiresAt() != null) {
            String t = String.valueOf(jwt.getExpiresAt().getTime());
            t = t.substring(0, t.length() - 3);
            this.jwtExpirationTime.setText(t);
        }
        if (jwt.getIssuedAt() != null) {
            String t = String.valueOf(jwt.getIssuedAt().getTime());
            t = t.substring(0, t.length() - 3);
            this.jwtIssuedAt.setText(t);
        }
        if (jwt.getNotBefore() != null) {
            String t = String.valueOf(jwt.getNotBefore().getTime());
            t = t.substring(0, t.length() - 3);
            this.jwtNotBefore.setText(t);
        }
        this.jwtID.setText(jwt.getId());
        Map<String, Claim> claims = jwt.getClaims();
        if (claims != null) {
            Map<String, Object> customClaims = new HashMap<>();
            Field[] registeredClaims = RegisteredClaims.class.getFields();
            List<String> registeredClaimName = new ArrayList<>(registeredClaims.length);
            try {
                for (Field registeredClaim : registeredClaims) {
                    if (registeredClaim.getType() == String.class) {
                        registeredClaimName.add((String) registeredClaim.get(null));
                    }
                }
            } catch (IllegalAccessException ignored) {}
            try {
                for (Map.Entry<String, Claim> entry : claims.entrySet()) {
                    if (!registeredClaimName.contains(entry.getKey())) {
                        Claim v = entry.getValue();
                        Field f = null;
                        Field[] fs = v.getClass().getDeclaredFields();
                        for (Field _f : fs) {
                            if (_f.getType() == JsonNode.class) {
                                f = _f;
                                f.setAccessible(true);
                                break;
                            }
                        }
                        if (f == null) {
                            throw new NoSuchFieldException("Class com.auth0.jwt.impl.JsonNodeClaim doesn't has JsonNode declared field.");
                        }
                        JsonNode jsonNode = (JsonNode) f.get(v);
                        if (v.isNull()) {
                            customClaims.put(entry.getKey(), null);
                        } else {
                            customClaims.put(entry.getKey(), jsonNode);
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.not.have.json.node.error"));
            }
            try {
                String s = JsonUtil.INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(customClaims).replace("\r\n", "\n");
                ApplicationManager.getApplication().runWriteAction(() -> {
                    CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                        this.jwtDecodedEditorEx.getDocument().setText(s);
                    });
                });
            } catch (JsonProcessingException ignored) {}
        } else {
            ApplicationManager.getApplication().runWriteAction(() -> {
                CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                    this.jwtDecodedEditorEx.getDocument().setText("");
                });
            });
        }
    }

//    private void withClaim(String key, Object value, JWTCreator.Builder builder) {
//        if (value instanceof Boolean) {
//            builder.withClaim(key, (Boolean) value);
//        } else if (value instanceof Integer) {
//            builder.withClaim(key, (Integer) value);
//        } else if (value instanceof Long) {
//            builder.withClaim(key, (Long) value);
//        } else if (value instanceof Double) {
//            builder.withClaim(key, (Double) value);
//        } else if (value instanceof String) {
//            builder.withClaim(key, (String) value);
//        } else if (value instanceof Map) {
//            builder.withClaim(key, (Map) value);
//        } else if (value instanceof List) {
//            builder.withClaim(key, (List) value);
//        } else {
//            lostKey.add(key);
//        }
//    }

    void verify() {
        //noinspection UseJBColor
        this.jwtVerifyStatusIcon.setBackground(new Color(0, 0, 0, 0));
        Algorithm alg = null;
        String selectedItem = (String) this.jwtSelectAlgorithm.getSelectedItem();
        if (selectedItem != null) {
            String priv = this.jwtPrivateKeyEditorEx.getDocument().getText();
            String pub = this.jwtPublicKeyEditorEx.getDocument().getText();
            switch (selectedItem) {
                case "HS256" -> alg = Algorithm.HMAC256(priv.isEmpty() ? pub : priv);
                case "HS384" -> alg = Algorithm.HMAC384(priv.isEmpty() ? pub : priv);
                case "HS512" -> alg = Algorithm.HMAC512(priv.isEmpty() ? pub : priv);
                case "RS256", "RS384", "RS512" -> {
                    try {
                        byte[] pubkeyEncoded = JWKUtil.pem2byte(pub);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        try {
                            pubkeyEncoded = Base64.getDecoder().decode(pubkeyEncoded);
                        } catch (IllegalArgumentException ignored) {}
                        RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubkeyEncoded));
                        switch (selectedItem) {
                            case "RS256" -> alg = Algorithm.RSA256(rsaPubKey);
                            case "RS384" -> alg = Algorithm.RSA384(rsaPubKey);
                            case "RS512" -> alg = Algorithm.RSA512(rsaPubKey);
                        }
                    } catch (Exception e) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                    }
                }
                case "ES256", "ES384", "ES512" -> {
                    try {
                        byte[] encoded = Base64.getDecoder().decode(pub);
                        KeyFactory kf = KeyFactory.getInstance("EC");
                        EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
                        ECPublicKey publicKey = (ECPublicKey) kf.generatePublic(keySpec);
                        switch (selectedItem) {
                            case "ES256" -> alg = Algorithm.ECDSA256(publicKey);
                            case "ES384" -> alg = Algorithm.ECDSA384(publicKey);
                            case "ES512" -> alg = Algorithm.ECDSA512(publicKey);
                        }
                    } catch (Exception e) {
                        this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                    }
                }
            }
        }
        if (alg == null) {
            this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.algorithm"));
            return;
        }
        Verification builder = JWT.require(alg);
        if (this.jwtIssuer.getText() != null && !this.jwtIssuer.getText().isEmpty()) {
            builder.withIssuer(this.jwtIssuer.getText());
        }
        if (this.jwtSubject.getText() != null && !this.jwtSubject.getText().isEmpty()) {
            builder.withSubject(this.jwtSubject.getText());
        }
        if (this.jwtAudience.getText() != null && !this.jwtAudience.getText().isEmpty()) {
            boolean jsonSuc = false;
            String audienceText = this.jwtAudience.getText();
            if (audienceText.startsWith("[")) {
                try {
                    List<String> list = JsonUtil.INSTANCE.readValue(audienceText, new TypeReference<List<String>>() {
                    });
                    jsonSuc = true;
                    builder.withAudience(list.toArray(new String[0]));
                } catch (JsonProcessingException e) {}
            }
            if (!jsonSuc) {
                builder.withAudience(this.jwtAudience.getText());
            }
        }
        if (this.jwtID.getText() != null && !this.jwtID.getText().isEmpty()) {
            builder.withJWTId(this.jwtID.getText());
        }
        if (this.jwtNotBefore.getText() != null && !this.jwtNotBefore.getText().isEmpty()) {
            builder.acceptNotBefore(Long.parseLong(this.jwtNotBefore.getText() + "000"));
        }
        if (this.jwtIssuedAt.getText() != null && !this.jwtIssuedAt.getText().isEmpty()) {
            builder.acceptIssuedAt(Long.parseLong(this.jwtIssuedAt.getText() + "000"));
        }
        if (this.jwtExpirationTime.getText() != null && !this.jwtExpirationTime.getText().isEmpty()) {
            builder.acceptExpiresAt(Long.parseLong(this.jwtExpirationTime.getText() + "000"));
        }
        String customClaims = this.jwtDecodedEditorEx.getDocument().getText();
        if (!customClaims.isEmpty()) {
            lostKey.clear();
            try {
                Map<String, Object> claims = JsonUtil.INSTANCE.readValue(customClaims, new TypeReference<Map<String, Object>>() {
                });
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        builder.withClaim(entry.getKey(), (Boolean) entry.getValue());
                    } else if (entry.getValue() instanceof Integer) {
                        builder.withClaim(entry.getKey(), (Integer) entry.getValue());
                    } else if (entry.getValue() instanceof Long) {
                        builder.withClaim(entry.getKey(), (Long) entry.getValue());
                    } else if (entry.getValue() instanceof Double) {
                        builder.withClaim(entry.getKey(), (Double) entry.getValue());
                    } else if (entry.getValue() instanceof String) {
                        builder.withClaim(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Map) {
                        builder.withClaim(entry.getKey(), new JWTBiPredicate<>((Map) entry.getValue()));
                    } else if (entry.getValue() instanceof List) {
                        builder.withClaim(entry.getKey(), new JWTBiPredicate<>((List) entry.getValue()));
                    } else {
                        lostKey.add(entry.getKey());
                    }
                }
            } catch (JsonProcessingException e) {
                this.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.wrong.claims.json.string"));
            } catch (Exception e) {
                this.jwtVerifyStatus.setText(e.getLocalizedMessage());
            }
        }
        JWTVerifier verifier = builder.build();
        try {
            verifier.verify(this.jwtEncodedEditorEx.getDocument().getText());
            this.jwtVerifyStatusIcon.setBackground(JBColor.GREEN);
        } catch (JWTVerificationException e) {
            this.jwtVerifyStatusIcon.setBackground(JBColor.RED);
        }
    }

    /**
     * 同时提供给编码区和解码区Editor的事件
     */
    class ADocumentListener implements DocumentListener {
        private final EditorEx editor;
        private final ToolWindowEncrypt component;
        public ADocumentListener(EditorEx editor, ToolWindowEncrypt component) {
            this.editor = editor;
            this.component = component;
        }
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            if (this.component.stateLock) {
                return;
            }
            ApplicationManager.getApplication().runWriteAction(() -> {
                // EDT
                this.component.stateLock = true;
                try {
                    if (this.editor.isDisposed()) {
                        return;
                    }
                    if (this.editor == this.component.jwtDecodedEditorEx) {
                        if (this.component.jwtAutoRun.isSelected()) {
                            this.component.encode();
                        }
                    } else if (this.editor == this.component.jwtEncodedEditorEx) {
                        if (this.component.jwtAutoRun.isSelected()) {
                            this.component.decode();
                        }
                    } else if (this.editor == this.component.jwtPrivateKeyEditorEx || this.editor == this.component.jwtPublicKeyEditorEx) {
                        if (this.component.jwtAutoRun.isSelected()) {
                            this.component.encode();
                        }
                    }
                    this.component.verify();
//                    String text = editor.getDocument().getText();
//                    String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
//                    if (tmp.length() < 1) {
//                        return;
//                    }
//                    tmp = tmp.substring(0, 1);
//                    if ("{".equals(tmp) || "[".equals(tmp)) {
//                        if (!"json".equals(lastFormat)) {
//                            useFormatToCreateEditor("json");
//                            lastFormat = "json";
//                        }
//                    } else if ("<".equals(tmp)) {
//                        if (!"xml".equals(lastFormat)) {
//                            useFormatToCreateEditor("xml");
//                            lastFormat = "xml";
//                        }
//                    }
                } finally {
                    this.component.stateLock = false;
                }
            });
        }
    }

}
