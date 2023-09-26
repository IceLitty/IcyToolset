package com.gmail.litalways.toolset.gui;

import com.gmail.litalways.toolset.util.JWKUtil;
import com.gmail.litalways.toolset.util.MessageUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JWT编解码
 *
 * @author IceRain
 * @since 2023/09/20
 */
public class MainFormEncryptJWTFunction {

    private final ToolWindowEncrypt component;

    public MainFormEncryptJWTFunction(ToolWindowEncrypt component) {
        this.component = component;
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
            if (!this.component.stateLock) {
                this.component.stateLock = true;
                try {
                    this.component.encode();
                    this.component.verify();
                } finally {
                    this.component.stateLock = false;
                }
            }
        });
        this.component.jwtEncodeBtn.addActionListener(e -> {
            if (!this.component.stateLock) {
                this.component.stateLock = true;
                try {
                    this.component.encode();
                    this.component.verify();
                } finally {
                    this.component.stateLock = false;
                }
            }
        });
        this.component.jwtDecodeBtn.addActionListener(e -> {
            if (!this.component.stateLock) {
                this.component.stateLock = true;
                try {
                    this.component.decode();
                    this.component.verify();
                } finally {
                    this.component.stateLock = false;
                }
            }
        });
        this.component.jwtCleanBtn.addActionListener(e -> {
            this.component.stateLock = true;
            try {
                clean();
            } finally {
                this.component.stateLock = false;
            }
        });
        this.component.jwtPublicKeyEditor.addMouseListener(new AMouseListener());
        this.component.jwtPrivateKeyEditor.addMouseListener(new AMouseListener());
        this.component.jwtPemJwkSwitchBtn.addActionListener(e -> {
            this.component.stateLock = true;
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
                this.component.stateLock = false;
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
        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                this.component.jwtDecodedEditorEx.getDocument().setText("");
                this.component.jwtEncodedEditorEx.getDocument().setText("");
                this.component.jwtPublicKeyEditorEx.getDocument().setText("");
                this.component.jwtPrivateKeyEditorEx.getDocument().setText("");
            });
        });
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
                if (privateKey.isBlank()) {
                } else if (privateKey.startsWith("{")) {
                    mode = 1;
                } else {
                    mode = 2;
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
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                            this.component.jwtPublicKeyEditorEx.getDocument().setText(publicJwk);
                        });
                    });
                }
                if (!privateKey.isBlank() && privateKey.startsWith("{")) {
                    String privateJwk = JWKUtil.jwk2pem(privateKey);
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                            this.component.jwtPrivateKeyEditorEx.getDocument().setText(privateJwk);
                        });
                    });
                }
            } else {
                // pem to jwk
                String alg = String.valueOf(this.component.jwtSelectAlgorithm.getSelectedItem());
                if (!publicKey.isBlank() && !publicKey.startsWith("{")) {
                    String publicJwk = JWKUtil.publicKey2jwk(JWKUtil.pem2byte(publicKey), alg, null, null);
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                            this.component.jwtPublicKeyEditorEx.getDocument().setText(publicJwk);
                        });
                    });
                }
                if (!privateKey.isBlank() && !privateKey.startsWith("{")) {
                    String privateJwk = JWKUtil.privateKey2jwk(JWKUtil.pem2byte(privateKey), alg, null, null);
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                            this.component.jwtPrivateKeyEditorEx.getDocument().setText(privateJwk);
                        });
                    });
                }
            }
        } catch (Exception e) {
            this.component.jwtVerifyStatus.setText(MessageUtil.getMessage("encrypt.jwt.tip.pem.jwk.convert.error", e.getLocalizedMessage()));
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
     * 提供给解码区TextField的编码事件
     */
    class BDocumentListener implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            if (component.jwtAutoRun.isSelected()) {
                if (component.stateLock) {
                    return;
                }
                component.stateLock = true;
                try {
                    component.encode();
                } finally {
                    component.stateLock = false;
                }
            }
        }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            if (component.jwtAutoRun.isSelected()) {
                if (component.stateLock) {
                    return;
                }
                component.stateLock = true;
                try {
                    component.encode();
                } finally {
                    component.stateLock = false;
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
                if (component.stateLock) {
                    return;
                }
                component.stateLock = true;
                try {
                    component.encode();
                } finally {
                    component.stateLock = false;
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
