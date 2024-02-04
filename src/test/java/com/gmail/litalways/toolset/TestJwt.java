package com.gmail.litalways.toolset;

import cn.hutool.http.HttpUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gmail.litalways.toolset.util.JWKUtil;
import com.gmail.litalways.toolset.util.JsonUtil;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Slf4j
public class TestJwt {

    @Test
    @Tag("test")
    public void testJwt() throws Exception {
        String mills = String.valueOf(new Date().getTime());
        log.info("{} | {}", mills, mills.substring(0, mills.length() - 3));
        //
        String token = "";
        DecodedJWT decodedJWT = JWT.decode(token);
        {
            Map<String, Object> customClaims = new HashMap<>();
            Claim v = decodedJWT.getClaim("custom");
            Field f = v.getClass().getDeclaredField("data");
            f.setAccessible(true);
            JsonNode jsonNode = (JsonNode) f.get(v);
            customClaims.put("custom", jsonNode);
            String s = JsonUtil.INSTANCE.writeValueAsString(customClaims);
            log.info(">>>>>>>>>> " + s);
        }
        {
            String s = HttpUtil.get("https://localhost/");
            log.info("<<<<<<<<<< " + s);
        }
        log.info(decodedJWT.getClaims().get("sub").getClass().getName());
        log.info(decodedJWT.getClaims().get("custom").getClass().getName());
        log.info("++++++");
        log.info(decodedJWT.getType());
        log.info(decodedJWT.getAlgorithm());
        log.info(decodedJWT.getSubject());
        log.info(String.valueOf(decodedJWT.getAudience()));
        log.info(decodedJWT.getIssuer());
        log.info(String.valueOf(decodedJWT.getExpiresAt()));
        log.info(String.valueOf(decodedJWT.getIssuedAt()));
        log.info(String.valueOf(decodedJWT.getNotBefore()));
        log.info(decodedJWT.getId());
        log.info(String.valueOf(decodedJWT.getClaims()));
        log.info(decodedJWT.getSignature());
//        log.info(decodedJWT.getHeaderClaim(""));
        log.info(decodedJWT.getContentType());
        log.info(decodedJWT.getKeyId());
        //
        String tokenO = JWT.create().withSubject("ccc").withClaim("custom", Collections.singletonMap("a", "aaaaa")).sign(Algorithm.HMAC256("asd"));
        log.info(tokenO);
        log.info(String.valueOf(JWT.decode(tokenO).getClaims()));
        // test private key
        String privKey = "-----BEGIN PRIVATE KEY-----\n" +
                "-----END PRIVATE KEY-----\n";
        String privKeyPkcs8 = "";
        String pubKey = "-----BEGIN PUBLIC KEY-----\n" +
                "-----END PUBLIC KEY-----\n";
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "-----END CERTIFICATE-----\n";
        //
        log.info("------------");
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] privkeyEncoded = Base64.getDecoder().decode(privKeyPkcs8);
        final RSAPrivateKey rsaPrivKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privkeyEncoded));
        final SubjectPublicKeyInfo pubKeyInfo = (SubjectPublicKeyInfo) new PEMParser(new StringReader(pubKey)).readObject();
        byte[] pubkeyEncoded = pubKeyInfo.getEncoded();
        final RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubkeyEncoded));
        String token1 = JWT.create().withSubject("asdasd").sign(Algorithm.RSA256(rsaPrivKey));
        log.info(token1);
        log.info(String.valueOf(JWT.decode(token1).getClaims()));
        DecodedJWT decodedJWT1 = JWT.require(Algorithm.RSA256(rsaPubKey)).build().verify(token1);
        log.info(decodedJWT1.getSignature());
        log.info("------------");
        //
        String jwkString = pem2jwk(pubKey, "RS256", cert, "SHA-1");
        log.info(jwkString);
        String pubKeyR = jwk2pem(jwkString);
        log.info(pubKeyR);
        log.info("-------------");
        //
        String privJwk = JWKUtil.privateKey2jwk(JWKUtil.pem2byte(privKey), "RS256", cert, "SHA-1");
        log.info(privJwk);
        log.info("=============");
        //
        byte[] pkcs1Private = JWKUtil.privateKeyPkcs8ToPkcs1(JWKUtil.pem2byte(privKey));
        log.info(Arrays.toString(pkcs1Private));
        byte[] pkcs8Private = JWKUtil.privateKeyPkcs1ToPkcs8(pkcs1Private);
        log.info(Arrays.toString(pkcs8Private));
        byte[] pkcs1Public = JWKUtil.publicKeyPkcs8ToPkcs1(JWKUtil.pem2byte(pubKey));
        log.info(Arrays.toString(pkcs1Public));
        byte[] pkcs8Public = JWKUtil.publicKeyPkcs1ToPkcs8(pkcs1Public);
        log.info(Arrays.toString(pkcs8Public));
        // load public key to EC
//        byte[] encoded = Base64.getDecoder().decode(pubKey);
//        KeyFactory kf = KeyFactory.getInstance(“EC”);
//        EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
//        ECPublicKey publicKey = (ECPublicKey)kf.generatePublic(keySpec);
    }

    public static String jwk2pem(final String jwkString) throws IllegalArgumentException {
        try {
            Map<String, Object> jwkMap = new JsonMapper().readValue(jwkString, new TypeReference<>() {
            });
            if (!jwkMap.containsKey("n") || !jwkMap.containsKey("e")) {
                throw new IllegalArgumentException("Give JWK not contain modulus and exponent.");
            }
            String n = (String) jwkMap.get("n");
            String e = (String) jwkMap.get("e");
            if (n == null || n.trim().isEmpty() || e == null || e.trim().isEmpty()) {
                throw new IllegalArgumentException("Give JWK not contain modulus and exponent.");
            }
            RSAKey rsaKey = new RSAKey.Builder(Base64URL.from(n), Base64URL.from(e)).build();
            PublicKey publicKey = rsaKey.toPublicKey();
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } catch (Exception e) {
            throw new IllegalArgumentException("JWK to PEM error", e);
        }
    }

    /**
     * Returns the JSON formatted JWK based on provided public key / cert
     * via <a href="https://stackoverflow.com/a/73703594">stackoverflow</a>
     *
     * @param rawKey - public key as read from disk in pem format
     * @param keyAlgorithm - RS256
     * @param cert - certificate as read from disk in pem format
     * @param certThumbprintAlgorithm - only SHA-1 or SHA-256
     * @return JWK Key as <String>
     */
    public static String pem2jwk(final String rawKey, final String keyAlgorithm, final String cert, final String certThumbprintAlgorithm) throws IllegalArgumentException {
        try {
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            final SubjectPublicKeyInfo pubKeyInfo = (SubjectPublicKeyInfo) new PEMParser(new StringReader(rawKey)).readObject();
            byte[] keyEncoded = pubKeyInfo.getEncoded();
            final X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(keyEncoded);
            final RSAPublicKey rsaPubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
            final RSAKey key;
            if (cert != null) {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                final X509Certificate rsaCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
                final byte[] certEncoded = rsaCert.getEncoded();
                final List<com.nimbusds.jose.util.Base64> certChain = new ArrayList<>();
                certChain.add(com.nimbusds.jose.util.Base64.encode(certEncoded));
                final MessageDigest md = MessageDigest.getInstance(certThumbprintAlgorithm);
                md.update(certEncoded);
                final String hexThumbprint = Hex.toHexString(md.digest());
                final Base64URL thumbprint = Base64URL.from(hexThumbprint);
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
        } catch (final Exception e) {
            throw new IllegalArgumentException("PEM to JWK error", e);
        }
    }

}
