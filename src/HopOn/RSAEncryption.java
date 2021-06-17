package HopOn;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

public class RSAEncryption {
    public static void main(String args[]) throws Exception {
        //Creating a Signature object
        Signature sign = Signature.getInstance("SHA256withRSA");

        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the key pair generator
        keyPairGen.initialize(2048);

        //Generating the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        //Creating a Cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        //Initializing a Cipher object
        cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());

        //Variable #1
        //Adding data to the cipher
        byte[] input = "Welcome to Tutorialspoint".getBytes();
        cipher.update(input);

        //Variable #2
        String str = ",abc123";
        byte[] byteArr = str.getBytes();
        // print the byte[] elements
        cipher.update(byteArr);
        //System.out.println("String to byte array: " + Arrays.toString(byteArr));

        //Variable #3
        String str2 = ",tabish123";
        byte[] byteArr2 = str2.getBytes();
        cipher.update(byteArr2);


        //encrypting the data
        byte[] cipherText = cipher.doFinal();
        System.out.println(new String(cipherText, StandardCharsets.UTF_8));

        //Initializing the same cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

        //Decrypting the text
        byte[] decipheredText = cipher.doFinal(cipherText);
        String str1 = new String(decipheredText);
        System.out.println(new String(decipheredText));

        String[] splitValues = str1.split(",");
        String Database = splitValues[0];
        String Database1 = splitValues[1];
        String Database2 = splitValues[2];
        System.out.println("1. " + Database);
        System.out.println("2. " + Database1);
        System.out.println("3. " + Database2);
    }
}
