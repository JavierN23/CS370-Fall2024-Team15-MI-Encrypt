import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionService {
    
private static final SecretKeySpec KEY =
            new SecretKeySpec("1234567890123456".getBytes(), "AES");

    public static String encryptCTR(String text) {
        try {
            byte[] nonce = new byte[8];
            new SecureRandom().nextBytes(nonce);

            byte[] iv = new byte[16];
            System.arraycopy(nonce, 0, iv, 0, 8);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(text.getBytes());

            byte[] output = new byte[8 + encrypted.length];
            System.arraycopy(nonce, 0, output, 0, 8);
            System.arraycopy(encrypted, 0, output, 8, encrypted.length);

            return Base64.getEncoder().encodeToString(output);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String decryptCTR(String text) {
        try {
            byte[] input = Base64.getDecoder().decode(text);

            byte[] nonce = new byte[8];
            System.arraycopy(input, 0, nonce, 0, 8);

            byte[] ciphertext = new byte[input.length - 8];
            System.arraycopy(input, 8, ciphertext, 0, ciphertext.length);

            byte[] iv = new byte[16];
            System.arraycopy(nonce, 0, iv, 0, 8);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(iv));

            return new String(cipher.doFinal(ciphertext));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}