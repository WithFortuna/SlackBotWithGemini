package com.zuzihe.slackbot.bolt.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Lightweight AES/GCM encryption for sensitive values (e.g., clientSecret, signingSecret).
 * Controlled by properties:
 *  - slack.crypto.enabled (default: false)
 *  - slack.crypto.key (required when enabled)
 */
@Component
public class SensitiveDataCrypto {
    private final boolean enabled;
    private final byte[] keyBytes;
    private final SecureRandom secureRandom = new SecureRandom();

    public SensitiveDataCrypto(
            @Value("${slack.crypto.enabled:false}") boolean enabled,
            @Value("${slack.crypto.key:}") String key
    ) {
        this.enabled = enabled && key != null && !key.isBlank();
        this.keyBytes = deriveAesKey(key);
    }

    private byte[] deriveAesKey(String key) {
        if (!enabled) return new byte[0];
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(key.getBytes(StandardCharsets.UTF_8));
            // Use 16 bytes (AES-128) from SHA-256 digest
            byte[] aesKey = new byte[16];
            System.arraycopy(digest, 0, aesKey, 0, 16);
            return aesKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize crypto key", e);
        }
    }

    public String encrypt(String plain) {
        if (!enabled || plain == null) return plain;
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            // Package as base64(iv + ciphertext)
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv).put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt sensitive field", e);
        }
    }

    public String decrypt(String wrapped) {
        if (!enabled || wrapped == null) return wrapped;
        try {
            byte[] all = Base64.getDecoder().decode(wrapped);
            byte[] iv = new byte[12];
            byte[] cipherText = new byte[all.length - iv.length];
            System.arraycopy(all, 0, iv, 0, iv.length);
            System.arraycopy(all, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt sensitive field", e);
        }
    }
}

