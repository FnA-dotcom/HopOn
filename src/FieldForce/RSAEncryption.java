package FieldForce;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class RSAEncryption {
    static final String TAG = "RSAEncryption";

    public static void main(String[] args) {
        String theTestText = "tabish@supernetesolutions.com";
        // Set up secret key spec for 128-bit AES encryption and decryption
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            System.out.println("AES secret key spec error");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(theTestText.getBytes());
        } catch (Exception e) {
            System.out.println("AES encryption error");
        }
        System.out.println("[ENCODED]:\n" +
                Base64.encodeBase64String(encodedBytes));
//        String encryptedValue = new BASE64Encoder().encode(encodedBytes);
//        System.out.println("encodedBytes-->" + encryptedValue);
    }
}
