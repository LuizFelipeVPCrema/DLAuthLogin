package org.setbk.dLAuthLogin.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class PasswordUtils {
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new SecureRandom();
    
    /**
     * Criptografa uma senha usando BCrypt ou SHA256
     */
    public static String hashPassword(String password, boolean useBCrypt, int bcryptRounds) {
        if (useBCrypt) {
            return BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds));
        } else {
            return hashSHA256(password);
        }
    }
    
    /**
     * Verifica se uma senha está correta
     */
    public static boolean verifyPassword(String password, String hash, boolean useBCrypt) {
        if (useBCrypt) {
            return BCrypt.checkpw(password, hash);
        } else {
            return hashSHA256(password).equals(hash);
        }
    }
    
    /**
     * Criptografa uma string usando SHA256
     */
    public static String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar com SHA256", e);
        }
    }
    
    /**
     * Verifica se uma senha é forte o suficiente
     * Sistema de força: 1-6 pontos
     * 1 = Muito fraca (apenas letras)
     * 2 = Fraca (letras + números)
     * 3 = Média (letras + números + 8+ caracteres)
     * 4 = Boa (letras + números + maiúsculas/minúsculas)
     * 5 = Forte (letras + números + caracteres especiais)
     * 6 = Muito forte (todos os critérios)
     */
    public static boolean isPasswordStrong(String password, int minStrength) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        boolean hasUpper = false;
        boolean hasLower = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (Character.isUpperCase(c)) {
                    hasUpper = true;
                } else {
                    hasLower = true;
                }
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        
        int strength = 0;
        
        // Ponto 1: Tem letras
        if (hasLetter) strength++;
        
        // Ponto 2: Tem números
        if (hasDigit) strength++;
        
        // Ponto 3: Tem pelo menos 8 caracteres
        if (password.length() >= 8) strength++;
        
        // Ponto 4: Tem maiúsculas E minúsculas
        if (hasUpper && hasLower) strength++;
        
        // Ponto 5: Tem caracteres especiais
        if (hasSpecial) strength++;
        
        // Ponto 6: Tem pelo menos 12 caracteres
        if (password.length() >= 12) strength++;
        
        return strength >= minStrength;
    }
    
    /**
     * Gera um token de sessão aleatório
     */
    public static String generateSessionToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }
    
    /**
     * Gera um salt aleatório
     */
    public static String generateSalt() {
        StringBuilder salt = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            salt.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return salt.toString();
    }
    
    /**
     * Criptografa uma senha com salt personalizado
     */
    public static String hashWithSalt(String password, String salt) {
        return hashSHA256(password + salt);
    }
    
    /**
     * Verifica se uma senha com salt está correta
     */
    public static boolean verifyWithSalt(String password, String salt, String hash) {
        return hashWithSalt(password, salt).equals(hash);
    }
    
    /**
     * Gera uma senha aleatória segura
     */
    public static String generateSecurePassword(int length) {
        StringBuilder password = new StringBuilder();
        
        // Garantir pelo menos um caractere de cada tipo
        password.append(CHARACTERS.charAt(RANDOM.nextInt(26))); // Letra maiúscula
        password.append(CHARACTERS.charAt(26 + RANDOM.nextInt(26))); // Letra minúscula
        password.append(CHARACTERS.charAt(52 + RANDOM.nextInt(10))); // Dígito
        
        // Preencher o resto
        for (int i = 3; i < length; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        
        // Embaralhar a senha
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Obtém a força de uma senha (0-10)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 0;
        }
        
        int strength = 0;
        
        // Comprimento
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        if (password.length() >= 16) strength++;
        
        // Complexidade
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        boolean hasUpper = false;
        boolean hasLower = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (Character.isUpperCase(c)) {
                    hasUpper = true;
                } else {
                    hasLower = true;
                }
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        
        if (hasLetter) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        if (hasUpper && hasLower) strength++;
        
        return Math.min(strength, 10);
    }
}
