package tv.wuaki.wuakigourmet;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Crypto {

    /**
     * A private password
     */
    private static String cryptoPass = "XMxas00213xA";

    /**
     * Get the Cipher instance
     *
     * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return It returns the Cipher instance
     */
    private static Cipher getCipher(int mode) {
        // TODO: Check if we can use a unique instance and only call init every time mode changes
        Cipher instance = null;
        try {
            DESKeySpec keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            // TODO: Check if cipher is thread safe
            instance = Cipher.getInstance("DES");
            instance.init(mode, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Encrypt a string value
     *
     * @param value The string to be encrypted
     * @return The crypted string
     */
    public static String encrypt(String value) {
        try {
            byte[] clearBytes = value.getBytes("UTF8");
            byte[] encryptedBytes = getCipher(Cipher.ENCRYPT_MODE).doFinal(clearBytes);
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Decrypt a parameter
     *
     * @param value The string to be decrypted
     * @return The decrypted string
     */
    public static String decrypt(String value) {
        try {
            byte[] encrypedBytes = Base64.decode(value, Base64.DEFAULT);
            byte[] decrypedBytes = getCipher(Cipher.DECRYPT_MODE).doFinal(encrypedBytes);
            return new String(decrypedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
