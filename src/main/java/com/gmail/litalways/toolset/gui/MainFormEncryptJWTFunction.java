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
import com.gmail.litalways.toolset.util.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JWT编解码
 *
 * @author IceRain
 * @since 2023/09/20
 */
public class MainFormEncryptJWTFunction {

    private final ToolWindowEncrypt component;

    /**
     * TRUE: encode
     * FALSE: decode
     */
    private boolean lastFunc;
    /**
     * TRUE: 阻止自动执行
     * FALSE: 恢复自动执行
     */
    private final AtomicBoolean stateLock;
    private final List<String> lostKey;

    public MainFormEncryptJWTFunction(ToolWindowEncrypt component) {
        this.lastFunc = true;
        this.stateLock = new AtomicBoolean(false);
        this.lostKey = new ArrayList<>();
        this.component = component;
        this.component.jwtEncodedDocumentListener = new ADocumentListener(this.component.jwtEncodedEditorEx, this.component);
        this.component.jwtEncodedEditorEx.getDocument().addDocumentListener(this.component.jwtEncodedDocumentListener);
        this.component.jwtDecodedDocumentListener = new ADocumentListener(this.component.jwtDecodedEditorEx, this.component);
        this.component.jwtDecodedEditorEx.getDocument().addDocumentListener(this.component.jwtDecodedDocumentListener);
        this.component.jwtPublicKeyDocumentListener = new ADocumentListener(this.component.jwtPublicKeyEditorEx, this.component);
        this.component.jwtPublicKeyEditorEx.getDocument().addDocumentListener(this.component.jwtPublicKeyDocumentListener);
        this.component.jwtPrivateKeyDocumentListener = new ADocumentListener(this.component.jwtPrivateKeyEditorEx, this.component);
        this.component.jwtPrivateKeyEditorEx.getDocument().addDocumentListener(this.component.jwtPrivateKeyDocumentListener);
        this.component.jwtIssuedAt.addFocusListener(new CFocusListener());
        this.component.jwtNotBefore.addFocusListener(new CFocusListener());
        this.component.jwtExpirationTime.addFocusListener(new CFocusListener());
        this.component.jwtIssuedAt.getDocument().addDocumentListener(new DDocumentListener());
        this.component.jwtNotBefore.getDocument().addDocumentListener(new DDocumentListener());
        this.component.jwtExpirationTime.getDocument().addDocumentListener(new DDocumentListener());
        this.component.jwtAudience.getDocument().addDocumentListener(new BDocumentListener());
        this.component.jwtIssuer.getDocument().addDocumentListener(new BDocumentListener());
        this.component.jwtSubject.getDocument().addDocumentListener(new BDocumentListener());
        this.component.jwtID.getDocument().addDocumentListener(new BDocumentListener());
        this.component.jwtSelectAlgorithm.addActionListener(e -> {
            if (this.stateLock.compareAndSet(false, true)) {
                try {
                    this.encode();
                    this.verify();
                } finally {
                    this.stateLock.compareAndSet(true, false);
                }
            }
        });
        this.component.jwtEncodeBtn.addActionListener(e -> {
            if (this.stateLock.compareAndSet(false, true)) {
                try {
                    this.encode();
                    this.verify();
                } finally {
                    this.stateLock.compareAndSet(true, false);
                }
            }
        });
        this.component.jwtDecodeBtn.addActionListener(e -> {
            if (this.stateLock.compareAndSet(false, true)) {
                try {
                    this.decode();
                    this.verify();
                } finally {
                    this.stateLock.compareAndSet(true, false);
                }
            }
        });
        this.component.jwtCleanBtn.addActionListener(e -> {
            this.stateLock.set(true);
            try {
                clean();
            } finally {
                this.stateLock.compareAndSet(true, false);
            }
        });
        this.component.jwtPublicKeyEditor.addMouseListener(new AMouseListener());
        this.component.jwtPrivateKeyEditor.addMouseListener(new AMouseListener());
        this.component.jwtPemJwkSwitchBtn.addActionListener(e -> {
            this.stateLock.set(true);
            try {
                Object sel = this.component.jwtSelectAlgorithm.getSelectedItem();
                if (sel != null) {
                    String alg = String.valueOf(sel);
                    // HMAC不进行转换
                    if (!alg.startsWith("HS")) {
                        this.switchPemAndJwk();
                    }
                }
            } finally {
                this.stateLock.compareAndSet(true, false);
            }
        });
    }

    private void clean() {
        this.component.jwtAutoRun.setSelected(false);
        this.component.jwtSelectAlgorithm.setSelectedIndex(0);
        this.component.jwtAudience.setText("");
        this.component.jwtIssuer.setText("");
        this.component.jwtSubject.setText("");
        this.component.jwtID.setText("");
        this.component.jwtIssuedAt.setText("");
        this.component.jwtNotBefore.setText("");
        this.component.jwtExpirationTime.setText("");
        ApplicationManager.getApplication().runWriteAction(() -> CommandProcessor.getInstance().runUndoTransparentAction(() -> {
            this.component.jwtDecodedEditorEx.getDocument().setText("");
            this.component.jwtEncodedEditorEx.getDocument().setText("");
            this.component.jwtPublicKeyEditorEx.getDocument().setText("");
            this.component.jwtPrivateKeyEditorEx.getDocument().setText("");
        }));
        this.component.jwtAutoRun.setSelected(true);
        this.component.jwtVerifyStatus.setText("");
        this.component.jwtVerifyStatusIcon.setBackground(null);
    }

    private void switchPemAndJwk() {
        try {
            String publicKey = this.component.jwtPublicKeyEditorEx.getDocument().getText();
            String privateKey = this.component.jwtPrivateKeyEditorEx.getDocument().getText();
            int mode = 0; // default is nothing
            if (publicKey.isBlank()) {
                if (!privateKey.isBlank()) {
                    if (privateKey.startsWith("{")) {
                        mode = 1;
                    } else {
                        mode = 2;
                    }
                }
            } else if (publicKey.startsWith("{")) {
                mode = 1; // jwk to pem
            } else {
                mode = 2; // pem to jwk
            }
            if (mode == 1) {
                // jwk to pem
                if (!publicKey.isBlank() && publicKey.startsWith("{")) {
                    String publicJwk = JWKUtil.jwk2pem(publicKey);
                    ApplicationManager.getApplication().runWriteAction(() ->
                            CommandProcessor.getInstance().runUndoTransparentAction(() ->
                                    this.component.jwtPublicKeyEditorEx.getDocument().setText(publicJwk)));
                }
                if (!privateKey.isBlank() && privateKey.startsWith("{")) {
                    String privateJwk = JWKUtil.jwk2pem(privateKey);
                    ApplicationManager.getApplication().runWriteAction(() ->
                            CommandProcessor.getInstance().runUndoTransparentAction(() ->
                                    this.component.jwtPrivateKeyEditorEx.getDocument().setText(privateJwk)));
                }
            } else {
                // pem to jwk
                String alg = String.valueOf(this.component.jwtSelectAlgorithm.getSelectedItem());
                if (!publicKey.isBlank() && !publicKey.startsWith("{")) {
                    String publicJwk = JWKUtil.publicKey2jwk(JWKUtil.pem2byte(publicKey), alg, null, null);
                    ApplicationManager.getApplication().runWriteAction(() ->
                            CommandProcessor.getInstance().runUndoTransparentAction(() ->
                                    this.component.jwtPublicKeyEditorEx.getDocument().setText(publicJwk)));
                }
                if (!privateKey.isBlank() && !privateKey.startsWith("{")) {
                    String privateJwk = JWKUtil.privateKey2jwk(JWKUtil.pem2byte(privateKey), alg, null, null);
                    ApplicationManager.getApplication().runWriteAction(() ->
                            CommandProcessor.getInstance().runUndoTransparentAction(() ->
                                    this.component.jwtPrivateKeyEditorEx.getDocument().setText(privateJwk)));
                }
            }
        } catch (Exception e) {
            this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.pem.jwk.convert.error", e.getLocalizedMessage()));
        }
    }

    public void encode() {
        lastFunc = true;
        this.component.jwtVerifyStatus.setText("");
        JWTCreator.Builder builder = JWT.create();
        if (this.component.jwtIssuer.getText() != null && !this.component.jwtIssuer.getText().isEmpty()) {
            builder.withIssuer(this.component.jwtIssuer.getText());
        }
        if (this.component.jwtSubject.getText() != null && !this.component.jwtSubject.getText().isEmpty()) {
            builder.withSubject(this.component.jwtSubject.getText());
        }
        if (this.component.jwtAudience.getText() != null && !this.component.jwtAudience.getText().isEmpty()) {
            boolean jsonSuc = false;
            String audienceText = this.component.jwtAudience.getText();
            if (audienceText.startsWith("[")) {
                try {
                    List<String> list = JsonUtil.INSTANCE.readValue(audienceText, new TypeReference<>() {
                    });
                    jsonSuc = true;
                    builder.withAudience(list.toArray(new String[0]));
                } catch (JsonProcessingException ignored) {}
            }
            if (!jsonSuc) {
                builder.withAudience(this.component.jwtAudience.getText());
            }
        }
        if (this.component.jwtID.getText() != null && !this.component.jwtID.getText().isEmpty()) {
            builder.withJWTId(this.component.jwtID.getText());
        }
        if (this.component.jwtNotBefore.getText() != null && !this.component.jwtNotBefore.getText().isEmpty()) {
            try {
                builder.withNotBefore(new Date(Long.parseLong(this.component.jwtNotBefore.getText() + "000")));
            } catch (NumberFormatException e) {
                this.component.jwtNotBefore.setText("");
                return;
            }
        }
        if (this.component.jwtIssuedAt.getText() != null && !this.component.jwtIssuedAt.getText().isEmpty()) {
            try {
                builder.withIssuedAt(new Date(Long.parseLong(this.component.jwtIssuedAt.getText() + "000")));
            } catch (NumberFormatException e) {
                this.component.jwtIssuedAt.setText("");
                return;
            }
        }
        if (this.component.jwtExpirationTime.getText() != null && !this.component.jwtExpirationTime.getText().isEmpty()) {
            try {
                builder.withExpiresAt(new Date(Long.parseLong(this.component.jwtExpirationTime.getText() + "000")));
            } catch (NumberFormatException e) {
                this.component.jwtExpirationTime.setText("");
                return;
            }
        }
        String customClaims = this.component.jwtDecodedEditorEx.getDocument().getText();
        if (!customClaims.isEmpty()) {
            lostKey.clear();
            try {
                Map<String, Object> claims = JsonUtil.INSTANCE.readValue(customClaims, new TypeReference<>() {
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
                this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.wrong.claims.json.string"));
            } catch (Exception e) {
                this.component.jwtVerifyStatus.setText(e.getLocalizedMessage());
            }
            if (!lostKey.isEmpty()) {
                NotificationUtil.warning(MessageUtil.getMessage("encrypt.jwt.tip.lost.key", String.join(", ", lostKey)));
            }
        }
        Algorithm alg = null;
        String selectedItem = (String) this.component.jwtSelectAlgorithm.getSelectedItem();
        if (selectedItem != null) {
            String priv = this.component.jwtPrivateKeyEditorEx.getDocument().getText();
            String pub = this.component.jwtPublicKeyEditorEx.getDocument().getText();
            switch (selectedItem) {
                case "HS256", "HS384", "HS512" -> {
                    if (priv.isBlank() && pub.isBlank()) {
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", ""));
                        return;
                    }
                }
                default -> {
                    if (priv.isBlank()) {
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", ""));
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
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
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
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                        return;
                    }
                }
            }
        }
        if (alg == null) {
            this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.algorithm"));
            return;
        }
        String token = builder.sign(alg);
        ApplicationManager.getApplication().runWriteAction(() ->
                CommandProcessor.getInstance().runUndoTransparentAction(() ->
                        this.component.jwtEncodedEditorEx.getDocument().setText(token)));
    }

    public void decode() {
        lastFunc = false;
        this.component.jwtVerifyStatus.setText("");
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(this.component.jwtEncodedEditorEx.getDocument().getText());
        } catch (JWTDecodeException e) {
            return;
        }
        this.component.jwtSelectAlgorithm.setSelectedItem(jwt.getAlgorithm());
        this.component.jwtSubject.setText(jwt.getSubject());
        List<String> audience = jwt.getAudience();
        if (audience != null && !audience.isEmpty()) {
            if (audience.size() == 1) {
                this.component.jwtAudience.setText(audience.getFirst());
            } else {
                try {
                    this.component.jwtAudience.setText(JsonUtil.INSTANCE.writeValueAsString(audience));
                } catch (JsonProcessingException e) {
                    this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.audience.json.write.error", e.getLocalizedMessage()));
                    return;
                }
            }
        }
        String issuer = jwt.getIssuer();
        if (issuer != null && !issuer.trim().isEmpty()) {
            this.component.jwtIssuer.setText(issuer);
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
//                        ApplicationManager.getApplication().runWriteAction(() -> {});
                            // EDT
                            this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.jwk.download.failed.error", url));
                        });
                    }
                    if (data != null) {
                        // 写入公钥JWK窗口
                        final String s;
                        String s1;
                        try {
                            Map<String, Object> test = JsonUtil.INSTANCE.readValue(data, new TypeReference<>() {
                            });
                            s1 = data;
                        } catch (JsonProcessingException ignored) {
                            s1 = "";
                        }
                        s = s1;
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            // EDT
                            CommandProcessor.getInstance().runUndoTransparentAction(() -> this.component.jwtPublicKeyEditorEx.getDocument().setText(s));
                        });
                    }
                });
            }
        }
        if (jwt.getExpiresAt() != null) {
            String t = String.valueOf(jwt.getExpiresAt().getTime());
            t = t.substring(0, t.length() - 3);
            this.component.jwtExpirationTime.setText(t);
        }
        if (jwt.getIssuedAt() != null) {
            String t = String.valueOf(jwt.getIssuedAt().getTime());
            t = t.substring(0, t.length() - 3);
            this.component.jwtIssuedAt.setText(t);
        }
        if (jwt.getNotBefore() != null) {
            String t = String.valueOf(jwt.getNotBefore().getTime());
            t = t.substring(0, t.length() - 3);
            this.component.jwtNotBefore.setText(t);
        }
        this.component.jwtID.setText(jwt.getId());
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
                this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.not.have.json.node.error"));
            }
            try {
                String s = JsonUtil.INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(customClaims).replace("\r\n", "\n");
                ApplicationManager.getApplication().runWriteAction(() ->
                        CommandProcessor.getInstance().runUndoTransparentAction(() ->
                                this.component.jwtDecodedEditorEx.getDocument().setText(s)));
            } catch (JsonProcessingException ignored) {}
        } else {
            ApplicationManager.getApplication().runWriteAction(() ->
                    CommandProcessor.getInstance().runUndoTransparentAction(() ->
                            this.component.jwtDecodedEditorEx.getDocument().setText("")));
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
        this.component.jwtVerifyStatusIcon.setBackground(new Color(0, 0, 0, 0));
        Algorithm alg = null;
        String selectedItem = (String) this.component.jwtSelectAlgorithm.getSelectedItem();
        if (selectedItem != null) {
            String priv = this.component.jwtPrivateKeyEditorEx.getDocument().getText();
            String pub = this.component.jwtPublicKeyEditorEx.getDocument().getText();
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
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
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
                        this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.keys", e.getLocalizedMessage()));
                    }
                }
            }
        }
        if (alg == null) {
            this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.algorithm"));
            return;
        }
        Verification builder = JWT.require(alg);
        if (this.component.jwtIssuer.getText() != null && !this.component.jwtIssuer.getText().isEmpty()) {
            builder.withIssuer(this.component.jwtIssuer.getText());
        }
        if (this.component.jwtSubject.getText() != null && !this.component.jwtSubject.getText().isEmpty()) {
            builder.withSubject(this.component.jwtSubject.getText());
        }
        if (this.component.jwtAudience.getText() != null && !this.component.jwtAudience.getText().isEmpty()) {
            boolean jsonSuc = false;
            String audienceText = this.component.jwtAudience.getText();
            if (audienceText.startsWith("[")) {
                try {
                    List<String> list = JsonUtil.INSTANCE.readValue(audienceText, new TypeReference<>() {
                    });
                    jsonSuc = true;
                    builder.withAudience(list.toArray(new String[0]));
                } catch (JsonProcessingException ignored) {}
            }
            if (!jsonSuc) {
                builder.withAudience(this.component.jwtAudience.getText());
            }
        }
        if (this.component.jwtID.getText() != null && !this.component.jwtID.getText().isEmpty()) {
            builder.withJWTId(this.component.jwtID.getText());
        }
        if (this.component.jwtNotBefore.getText() != null && !this.component.jwtNotBefore.getText().isEmpty()) {
            builder.acceptNotBefore(Long.parseLong(this.component.jwtNotBefore.getText() + "000"));
        }
        if (this.component.jwtIssuedAt.getText() != null && !this.component.jwtIssuedAt.getText().isEmpty()) {
            builder.acceptIssuedAt(Long.parseLong(this.component.jwtIssuedAt.getText() + "000"));
        }
        if (this.component.jwtExpirationTime.getText() != null && !this.component.jwtExpirationTime.getText().isEmpty()) {
            builder.acceptExpiresAt(Long.parseLong(this.component.jwtExpirationTime.getText() + "000"));
        }
        String customClaims = this.component.jwtDecodedEditorEx.getDocument().getText();
        if (!customClaims.isEmpty()) {
            lostKey.clear();
            try {
                Map<String, Object> claims = JsonUtil.INSTANCE.readValue(customClaims, new TypeReference<>() {
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
                this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.wrong.claims.json.string"));
            } catch (Exception e) {
                this.component.jwtVerifyStatus.setText(e.getLocalizedMessage());
            }
            if (!lostKey.isEmpty()) {
                NotificationUtil.warning(MessageUtil.getMessage("encrypt.jwt.tip.lost.key", String.join(", ", lostKey)));
            }
        }
        JWTVerifier verifier = builder.build();
        try {
            verifier.verify(this.component.jwtEncodedEditorEx.getDocument().getText());
            this.component.jwtVerifyStatusIcon.setBackground(JBColor.GREEN);
        } catch (JWTVerificationException e) {
            this.component.jwtVerifyStatusIcon.setBackground(JBColor.RED);
        }
    }

    class AMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mousePressed(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            if (e.getComponent() == component.jwtPublicKeyEditor) {
                component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.editor.public.tooltip"));
            } else if (e.getComponent() == component.jwtPrivateKeyEditor) {
                component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.editor.private.tooltip"));
            }
        }
        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getComponent() == component.jwtPublicKeyEditor) {
                if (MessageUtil.getMessage("encrypt.jwt.editor.public.tooltip").equals(component.jwtVerifyStatus.getText())) {
                    component.jwtVerifyStatus.setText(null);
                }
            } else if (e.getComponent() == component.jwtPrivateKeyEditor) {
                if (MessageUtil.getMessage("encrypt.jwt.editor.private.tooltip").equals(component.jwtVerifyStatus.getText())) {
                    component.jwtVerifyStatus.setText(null);
                }
            }
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
            if (stateLock.compareAndSet(false, true)) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    // EDT
                    try {
                        if (this.editor.isDisposed()) {
                            return;
                        }
                        if (this.editor == this.component.jwtDecodedEditorEx) {
                            if (this.component.jwtAutoRun.isSelected()) {
                                encode();
                            }
                        } else if (this.editor == this.component.jwtEncodedEditorEx) {
                            if (this.component.jwtAutoRun.isSelected()) {
                                decode();
                            }
                        } else if (this.editor == this.component.jwtPrivateKeyEditorEx || this.editor == this.component.jwtPublicKeyEditorEx) {
                            if (this.component.jwtAutoRun.isSelected()) {
                                encode();
                            }
                        }
                        verify();
//                        String text = editor.getDocument().getText();
//                        String tmp = text.replace("\t", "").replace("\r", "").replace("\n", "").trim();
//                        if (tmp.length() < 1) {
//                            return;
//                        }
//                        tmp = tmp.substring(0, 1);
//                        if ("{".equals(tmp) || "[".equals(tmp)) {
//                            if (!"json".equals(lastFormat)) {
//                                useFormatToCreateEditor("json");
//                                lastFormat = "json";
//                            }
//                        } else if ("<".equals(tmp)) {
//                            if (!"xml".equals(lastFormat)) {
//                                useFormatToCreateEditor("xml");
//                                lastFormat = "xml";
//                            }
//                        }
                    } finally {
                        stateLock.compareAndSet(true, false);
                    }
                });
            }
        }
    }

    /**
     * 提供给解码区TextField的编码事件
     */
    class BDocumentListener implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            if (component.jwtAutoRun.isSelected()) {
                if (stateLock.compareAndSet(false, true)) {
                    try {
                        encode();
                    } finally {
                        stateLock.compareAndSet(true, false);
                    }
                }
            }
        }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            if (component.jwtAutoRun.isSelected()) {
                if (stateLock.compareAndSet(false, true)) {
                    try {
                        encode();
                    } finally {
                        stateLock.compareAndSet(true, false);
                    }
                }
            }
        }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
        }
    }

    /**
     * 提供给解码区FormattedTextField的编码事件
     */
    class CFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
        }
        @Override
        public void focusLost(FocusEvent e) {
            if (component.jwtAutoRun.isSelected()) {
                if (stateLock.compareAndSet(false, true)) {
                    try {
                        encode();
                    } finally {
                        stateLock.compareAndSet(true, false);
                    }
                }
            }
        }
    }

    /**
     * 提供给解码区FormattedTextField的实时显示格式化时间事件
     */
    class DDocumentListener implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            toFormat(e);
        }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            toFormat(e);
        }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
        }
        private void toFormat(javax.swing.event.DocumentEvent e) {
            Document document = e.getDocument();
            if (document != null) {
                try {
                    String epochTime = document.getText(0, document.getLength());
                    if (epochTime.replaceAll("\\D", "").length() == epochTime.length()) {
                        Date time = new Date(Long.parseLong(epochTime + "000"));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z zzzz");
                        String format = sdf.format(time);
                        component.jwtVerifyStatus.setText(format);
                    }
                } catch (BadLocationException ignored) {}
            }
        }
    }

}
