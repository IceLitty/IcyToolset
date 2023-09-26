package com.gmail.litalways.toolset.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JWK预处理工具类
 * 传入rawKey使用RSA/PKCS-1(.pem)
 *
 * @author IceRain
 * @since 2023/09/21
 */
public class JWKUtil {

    /**
     * 密钥文件读取为字节
     *
     * @param rawKey 密钥文本
     * @return 密钥字节
     * @throws IOException 读取失败，可能原因为密钥格式不支持
     */
    public static byte[] pem2byte(String rawKey) throws IOException {
        // SubjectPublicKeyInfo / PrivateKeyInfo
        Encodable encodable = (Encodable) new PEMParser(new StringReader(rawKey)).readObject();
        return encodable.getEncoded();
    }

    /**
     * 公钥PKCS-8转为PKCS-1
     *
     * @param key PKCS-8公钥
     * @return PKCS-1公钥
     */
    public static byte[] publicKeyPkcs8ToPkcs1(byte[] key) {
        ASN1Sequence publicKeyASN1Object = ASN1Sequence.getInstance(key);
        ASN1Encodable derBitStringASN1Encodable = publicKeyASN1Object.getObjectAt(1);
        DERBitString derBitStringObject = DERBitString.getInstance(derBitStringASN1Encodable);
        return derBitStringObject.getBytes();
    }

    /**
     * 公钥PKCS-1转为PKCS-8
     *
     * @param key PKCS-1公钥
     * @return PKCS-8公钥
     * @throws NoSuchAlgorithmException 无RSA实例
     * @throws InvalidKeySpecException  KEY异常
     */
    public static byte[] publicKeyPkcs1ToPkcs8(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        org.bouncycastle.asn1.pkcs.RSAPublicKey rsaPub = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey generatePublic = kf.generatePublic(new RSAPublicKeySpec(rsaPub.getModulus(), rsaPub.getPublicExponent()));
        return generatePublic.getEncoded();
    }

    /**
     * 私钥PKCS-8转为PKCS-1
     *
     * @param key PKCS-8私钥
     * @return PKCS-1私钥
     * @throws IOException 转换异常
     */
    public static byte[] privateKeyPkcs8ToPkcs1(byte[] key) throws IOException {
//        pkPair.getPrivateKey().getEncodedKey(); // java.security 返回的就是pkcs8对象
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(key);
        ASN1Encodable privateKeyPKCS1ASN1Encodable = pkInfo.parsePrivateKey();
        ASN1Primitive privateKeyPKCS1ASN1 = privateKeyPKCS1ASN1Encodable.toASN1Primitive();
        return privateKeyPKCS1ASN1.getEncoded();
    }

    /**
     * 私钥PKCS-1转为PKCS-8
     *
     * @param key PKCS-1私钥
     * @return PKCS-8私钥
     * @throws IOException 转换异常
     */
    public static byte[] privateKeyPkcs1ToPkcs8(byte[] key) throws IOException {
        org.bouncycastle.asn1.pkcs.RSAPrivateKey privateKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(key);
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.pkcs8ShroudedKeyBag);
        PrivateKeyInfo privKeyInfo = new PrivateKeyInfo(algorithmIdentifier, privateKey);
        return privKeyInfo.getEncoded();
    }

    /**
     * JWK转换为公/私钥
     *
     * @param jwkString JWK
     * @return PEM
     * @throws IllegalArgumentException 转换失败
     */
    public static String jwk2pem(final String jwkString) throws IllegalArgumentException {
        try {
            boolean privKey = false;
            Map<String, Object> jwkMap = new JsonMapper().readValue(jwkString, new TypeReference<Map<String, Object>>() {
            });
            if (!jwkMap.containsKey("n") || !jwkMap.containsKey("e")) {
                throw new IllegalArgumentException("Give JWK not contain modulus and exponent.");
            }
            String n = (String) jwkMap.get("n");
            String e = (String) jwkMap.get("e");
            if (n == null || n.trim().isEmpty() || e == null || e.trim().isEmpty()) {
                throw new IllegalArgumentException("Give JWK not contain modulus and exponent.");
            }
            RSAKey.Builder builder = new RSAKey.Builder(Base64URL.from(n), Base64URL.from(e));
            String d = (String) jwkMap.get("d");
            String dp = (String) jwkMap.get("dp");
            String dq = (String) jwkMap.get("dq");
            String p = (String) jwkMap.get("p");
            String q = (String) jwkMap.get("q");
            String qi = (String) jwkMap.get("qi");
            if (d != null && !d.trim().isEmpty()) {
                builder.privateExponent(Base64URL.from(d));
                privKey = true;
            }
            if (dp != null && !dp.trim().isEmpty()) {
                builder.firstFactorCRTExponent(Base64URL.from(dp));
            }
            if (dq != null && !dq.trim().isEmpty()) {
                builder.secondFactorCRTExponent(Base64URL.from(dq));
            }
            if (p != null && !p.trim().isEmpty()) {
                builder.firstPrimeFactor(Base64URL.from(p));
            }
            if (q != null && !q.trim().isEmpty()) {
                builder.secondPrimeFactor(Base64URL.from(q));
            }
            if (qi != null && !qi.trim().isEmpty()) {
                builder.firstCRTCoefficient(Base64URL.from(qi));
            }
            RSAKey rsaKey = builder.build();
            Key key;
            if (privKey) {
                key = rsaKey.toPrivateKey();
            } else {
                key = rsaKey.toPublicKey();
            }
            String encoded = java.util.Base64.getEncoder().encodeToString(key.getEncoded());
            if (privKey)
                encoded = "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----\n";
            else
                encoded = "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----\n";
            return encoded;
        } catch (Exception e) {
            throw new IllegalArgumentException("JWK to PEM error", e);
        }
    }

    /**
     * 将公钥与证书文件转换为JWK格式文本
     * via <a href="https://stackoverflow.com/a/73703594">stackoverflow</a>
     *
     * @param publicKey               公钥
     * @param keyAlgorithm            公钥算法，RS256
     * @param cert                    证书
     * @param certThumbprintAlgorithm 证书指纹算法，SHA-1，SHA-256
     * @return JWK
     * @throws IllegalArgumentException 转换失败
     */
    public static String publicKey2jwk(byte[] publicKey, String keyAlgorithm, String cert, String certThumbprintAlgorithm) throws IllegalArgumentException {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKey);
            RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
            RSAKey key;
            if (cert != null) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate rsaCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
                byte[] certEncoded = rsaCert.getEncoded();
                List<com.nimbusds.jose.util.Base64> certChain = new ArrayList<>();
                certChain.add(com.nimbusds.jose.util.Base64.encode(certEncoded));
                MessageDigest md = MessageDigest.getInstance(certThumbprintAlgorithm);
                md.update(certEncoded);
                String hexThumbprint = Hex.toHexString(md.digest());
                Base64URL thumbprint = Base64URL.from(hexThumbprint);
                switch (certThumbprintAlgorithm) {
                    case "SHA-1" -> key = new RSAKey.Builder(rsaPubKey)
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                            .x509CertChain(certChain)
                            .x509CertThumbprint(thumbprint)
                            .build();
                    case "SHA-256" -> key = new RSAKey.Builder(rsaPubKey)
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                            .x509CertChain(certChain)
                            .x509CertSHA256Thumbprint(thumbprint)
                            .build();
                    default -> throw new RuntimeException("Unsupported cert thumbprint algorithm.");
                }
            } else {
                key = new RSAKey.Builder(rsaPubKey)
                        .keyUse(KeyUse.SIGNATURE)
                        .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                        .build();
            }
            return key.toJSONString();
        } catch (Exception e) {
            throw new IllegalArgumentException("DER Public Key to JWK error", e);
        }
    }

    /**
     * 将私钥与证书文件转换为JWK格式文本
     *
     * @param privateKey              私钥
     * @param keyAlgorithm            私钥算法，RS256
     * @param cert                    证书
     * @param certThumbprintAlgorithm 证书指纹算法，SHA-1，SHA-256
     * @return JWK
     * @throws IllegalArgumentException 转换失败
     */
    public static String privateKey2jwk(byte[] privateKey, String keyAlgorithm, String cert, String certThumbprintAlgorithm) throws IllegalArgumentException {
        try {
//            java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey rsaPrivKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivateKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(privateKeyPkcs8ToPkcs1(privateKey));
            RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent()));
            RSAKey key;
            if (cert != null) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate rsaCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
                byte[] certEncoded = rsaCert.getEncoded();
                List<com.nimbusds.jose.util.Base64> certChain = new ArrayList<>();
                certChain.add(com.nimbusds.jose.util.Base64.encode(certEncoded));
                MessageDigest md = MessageDigest.getInstance(certThumbprintAlgorithm);
                md.update(certEncoded);
                String hexThumbprint = Hex.toHexString(md.digest());
                Base64URL thumbprint = Base64URL.from(hexThumbprint);
                switch (certThumbprintAlgorithm) {
                    case "SHA-1" -> key = new RSAKey.Builder(rsaPubKey)
                            .privateKey(rsaPrivKey)
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                            .x509CertChain(certChain)
                            .x509CertThumbprint(thumbprint)
                            .build();
                    case "SHA-256" -> key = new RSAKey.Builder(rsaPubKey)
                            .privateKey(rsaPrivKey)
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                            .x509CertChain(certChain)
                            .x509CertSHA256Thumbprint(thumbprint)
                            .build();
                    default -> throw new RuntimeException("Unsupported cert thumbprint algorithm.");
                }
            } else {
                key = new RSAKey.Builder(rsaPubKey)
                        .privateKey(rsaPrivKey)
                        .keyUse(KeyUse.SIGNATURE)
                        .algorithm(new com.nimbusds.jose.Algorithm(keyAlgorithm))
                        .build();
            }
            return key.toJSONString();
        } catch (Exception e) {
            throw new IllegalArgumentException("DER Private Key to JWK error", e);
        }
    }

}
