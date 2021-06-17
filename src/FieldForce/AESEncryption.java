package FieldForce;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("Duplicates")
public class AESEncryption {

    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        String originalString = "GETINPUT";
        System.out.println("Original String to encrypt - " + originalString);
        String encryptedString = encrypt(originalString);
        encryptedString = "P" + encryptedString;
        System.out.println("Encrypted String - " + encryptedString);
        if (encryptedString.startsWith("P")) {
            System.out.println("IN IF");
            encryptedString = encryptedString.substring(1);
            System.out.println("TABISH String - " + encryptedString);
            String decryptedString = decrypt(encryptedString);
            System.out.println("After decryption - " + decryptedString);
        } else {
            System.out.println("IN ELSE");
            String decryptedString = decrypt(encryptedString);
            System.out.println("After decryption - " + decryptedString);
        }

    }
}
