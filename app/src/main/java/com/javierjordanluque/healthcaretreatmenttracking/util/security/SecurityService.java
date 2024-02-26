package com.javierjordanluque.healthcaretreatmenttracking.util.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SecurityService {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "HealthcareTreatmentTracking_AESKey";
    private static final String HASH_ALGORITHM = "SHA-256";

    private static SecretKey secretKey;

    public static CipherData encrypt(byte[] data) throws Exception {
        if (secretKey == null)
            secretKey = getKey();

        IvParameterSpec iv = generateIV();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encryptedData = cipher.doFinal(data);

        return new CipherData(encryptedData, iv.getIV());
    }

    public static byte[] decrypt(CipherData encryptedData) throws Exception {
        IvParameterSpec ivSpec = new IvParameterSpec(encryptedData.getInitializationVector());
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return cipher.doFinal(encryptedData.getEncryptedData());
    }

    private static SecretKey getKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build();
            keyGenerator.init(keySpec);
            keyGenerator.generateKey();
        }

        return ((SecretKey) keyStore.getKey(KEY_ALIAS, null));
    }

    private static IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        return new IvParameterSpec(iv);
    }

    public static HashData hashPasswordWithSalt(String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        byte[] hashedPassword = hash(concatenateBytes(password.getBytes(), salt));

        return new HashData(hashedPassword, salt);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    private static byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);

        return messageDigest.digest(data);
    }


    private static byte[] concatenateBytes(byte[] dataA, byte[] dataB) {
        byte[] result = Arrays.copyOf(dataA, dataA.length + dataB.length);
        System.arraycopy(dataB, 0, result, dataA.length, dataB.length);

        return result;
    }

    public static boolean meetsPasswordRequirements(String password) {
        if (password.length() < 8) {
            return false;
        }

        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}