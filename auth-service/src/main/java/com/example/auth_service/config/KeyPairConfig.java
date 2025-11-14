package com.tours.authservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@Slf4j
public class KeyPairConfig {

    @Value("${security.jwt.key-store}")
    private String keyStore;

    @Bean
    public KeyPair keyPair() {
        try {
            // Cargar clave privada
            ClassPathResource privateKeyResource = new ClassPathResource("keys/keypair.pem");
            String privateKeyPEM = readPEMFile(privateKeyResource.getInputStream());
            PrivateKey privateKey = loadPrivateKey(privateKeyPEM);

            // Cargar clave pública
            ClassPathResource publicKeyResource = new ClassPathResource("keys/publickey.pem");
            String publicKeyPEM = readPEMFile(publicKeyResource.getInputStream());
            PublicKey publicKey = loadPublicKey(publicKeyPEM);

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            log.error("Error loading key pair", e);
            throw new RuntimeException("Error loading key pair", e);
        }
    }

    private String readPEMFile(InputStream inputStream) throws Exception {
        byte[] content = inputStream.readAllBytes();
        String pem = new String(content);
        return pem.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replace("-----BEGIN PUBLIC KEY-----", "")
                 .replace("-----END PUBLIC KEY-----", "")
                 .replaceAll("\\s", "");
    }

    private PrivateKey loadPrivateKey(String privateKeyPEM) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String publicKeyPEM) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}