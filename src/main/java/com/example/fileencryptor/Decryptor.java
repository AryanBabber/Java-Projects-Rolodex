package com.example.fileencryptor;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Decryptor {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final String KEY_DERIVATION = "PBKDF2WithHmacSHA512";

    public void decrypt(Path in, Path out, char[] password)
        throws GeneralSecurityException, IOException
    {
        try (
                InputStream fis = Files.newInputStream(in);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_SIZE];
            bis.read(salt);
            bis.read(iv);

            SecretKey secretKey = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            try (
                    OutputStream fos = Files.newOutputStream(out);
                    CipherOutputStream cos = new CipherOutputStream(fos, cipher)
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ( ( bytesRead = bis.read(buffer) ) != -1 ) {
                    cos.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private SecretKey deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_SIZE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
    }
}
