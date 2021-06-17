package FalconSchedulers;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionDecryption {
    private static final String ALGO = "AES";
    /*    private static final byte[] keyValue =
                new byte[] {  '(' , 'T', '@', 'b', '!' , 's', 'H',
                            '!' , '$' , 'T' , '#' ,'E' , 'b' , '3' , '$' , 'T' , ')' };*/
    private static final byte[] keyValue =
            new byte[]{'T', '#', '3', 'B', '3', '$', 'T',
                    '$', '3', 'C', 'r', '3', 't', 'K', '3', 'Q'};

    private static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.trim().getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    private static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData.trim());
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    public static void main(String[] args) {
        try {
            //String Enc = EncryptionDecryption.encrypt("3787-5085-7421-618_");//Card 1
            //String Enc = EncryptionDecryption.encrypt("4264-2879-0450-8997");//Card 2
            //String Enc = EncryptionDecryption.encrypt("1901");//Card 1 CVV
            String Enc = EncryptionDecryption.encrypt("abc123");//Card 2 CVV
            //String DEC = EncryptionDecryption.decrypt("NYwrb/Exfglgcp/76YDy0Q==");
            //String DEC = EncryptionDecryption.decrypt("7E40qBzNxzO/wK0eYcjWuw==");//Tabish id PWD
            String DEC = EncryptionDecryption.decrypt("c5SNHkrT+kDwqKk83dIzKA==");
            System.out.println("Encrypted Val ---- " + Enc);
            System.out.println("Decrypted Val ---- " + DEC);
            //String PWD = "Test@12345";
            //System.out.println("Hashed PWD " + encryptHash(PWD));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encryptHash(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {
        //			    StringBuilder buf = new StringBuilder();
        //			    byte[] arrayOfByte = data;int j = data.length;
        //			    for (int i = 0; i < j; i++)
        //			    {
        //			      byte b = arrayOfByte[i];
        //			      int halfbyte = b >>> 4 & 0xF;
        //			      int two_halfs = 0;
        //			      do
        //			      {
        //			        buf.append((halfbyte >= 0) && (halfbyte <= 9) ? (char)(48 + halfbyte) :
        //			          (char)(97 + (halfbyte - 10)));
        //			        halfbyte = b & 0xF;
        //			      } while (
        //
        //
        //
        //			        two_halfs++ < 1);
        //			    }
        //			    return buf.toString();
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

}
