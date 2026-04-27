import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionService {

    // AES Key
private static final SecretKeySpec KEY =
            new SecretKeySpec("1234567890123456".getBytes(), "AES");

            // Encrypts text using AES in CTR Mode
    public static String encryptCTR(String text) {
        try {
            byte[] nonce = new byte[8];
            new SecureRandom().nextBytes(nonce);

            byte[] iv = new byte[16];
            System.arraycopy(nonce, 0, iv, 0, 8);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(iv));

            // Encrypt the text
            byte[] encrypted = cipher.doFinal(text.getBytes());

            // Combine nonce + encrypted data
            byte[] output = new byte[8 + encrypted.length];
            System.arraycopy(nonce, 0, output, 0, 8);
            System.arraycopy(encrypted, 0, output, 8, encrypted.length);

            // Encode result as Based64 string
            return Base64.getEncoder().encodeToString(output);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Decrypts text using AES 
    public static String decryptCTR(String text) {
        try {
            byte[] input = Base64.getDecoder().decode(text);

            // Decode Base64 input
            byte[] nonce = new byte[8];
            System.arraycopy(input, 0, nonce, 0, 8);

            // Extract nonce
            byte[] ciphertext = new byte[input.length - 8];
            System.arraycopy(input, 8, ciphertext, 0, ciphertext.length);

            // Rebuild IV using nonce
            byte[] iv = new byte[16];
            System.arraycopy(nonce, 0, iv, 0, 8);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(iv));

            // Decrypt and return to original text
            return new String(cipher.doFinal(ciphertext));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}