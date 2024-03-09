package com.javierjordanluque.healthcaretreatmenttracking.util.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.DecryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.EncryptionException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.HashException;
import com.javierjordanluque.healthcaretreatmenttracking.util.exceptions.SecretKeyException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SecurityService {
    private static final String ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    private static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    private static final String PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    private static final String TRANSFORMATION = ALGORITHM + "/" + BLOCK_MODE + "/" + PADDING;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "HealthcareTreatmentTracking_AESKey";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static SecretKey secretKey;

    public static CipherData encrypt(byte[] data) throws EncryptionException {
        try {
            if (secretKey == null)
                secretKey = getKey();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data);

            return new CipherData(encryptedData, cipher.getIV());
        } catch (SecretKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException exception) {
            throw new EncryptionException("Failed to encrypt data (" + new String(data, StandardCharsets.UTF_8) + ")", exception);
        }
    }

    public static byte[] decrypt(CipherData cipherData) throws DecryptionException {
        try {
            if (secretKey == null)
                secretKey = getKey();

            IvParameterSpec ivSpec = new IvParameterSpec(cipherData.getInitializationVector());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return cipher.doFinal(cipherData.getEncryptedData());
        } catch (SecretKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException exception) {
            throw new DecryptionException("Failed to decrypt data (" + Arrays.toString(cipherData.getEncryptedData()) + ") " +
                    " with IV (" + Arrays.toString(cipherData.getInitializationVector()) + ")", exception);
        }
    }

    private static SecretKey getKey() throws SecretKeyException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(true)
                        .build();
                keyGenerator.init(keySpec);
                keyGenerator.generateKey();
            }

            return ((SecretKey) keyStore.getKey(KEY_ALIAS, null));
        } catch (InvalidAlgorithmParameterException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException exception) {
            throw new SecretKeyException("Failed to getKey " + ALGORITHM + " secret key", exception);
        }
    }

    public static HashData hashWithSalt(byte[] data) throws HashException {
        byte[] salt = generateSalt();
        byte[] hashedData = hash(concatenateBytes(data, salt));

        return new HashData(hashedData, salt);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    public static byte[] hash(byte[] data) throws HashException {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM).digest(data);
        } catch (NoSuchAlgorithmException exception) {
            throw new HashException("Failed to hash data (" + new String(data, StandardCharsets.UTF_8) + ") with " + HASH_ALGORITHM, exception);
        }
    }

    private static byte[] concatenateBytes(byte[] dataA, byte[] dataB) {
        byte[] result = Arrays.copyOf(dataA, dataA.length + dataB.length);
        System.arraycopy(dataB, 0, result, dataA.length, dataB.length);

        return result;
    }

    public static boolean equalsHashAndData(byte[] hashedData, byte[] salt, byte[] data) throws HashException {
        byte[] newHash = (salt == null) ? hash(data) : hash(concatenateBytes(data, salt));

        return Arrays.equals(newHash, hashedData);
    }

    public static boolean meetsPasswordRequirements(String password) {
        if (password.length() < 8)
            return false;

        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}
