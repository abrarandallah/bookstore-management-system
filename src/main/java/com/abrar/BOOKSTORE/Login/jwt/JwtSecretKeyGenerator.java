package com.abrar.BOOKSTORE.Login.jwt;
import java.security.SecureRandom;
import java.util.Base64;


public class JwtSecretKeyGenerator {

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[64];
        random.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }
}
