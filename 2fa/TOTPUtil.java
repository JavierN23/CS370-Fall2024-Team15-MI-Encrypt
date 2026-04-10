import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 TOTPUtil - Time-based One Time Password
 No external libraries required, using only javax.crypto in the JDK
 Compatible with Google Authenticator, Authy, Microsoft Authenticator, etc.
 */
public class TOTPUtil {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTES = 20;     // 160-bit secret
    private static final int TIME_STEP_SECS = 30;   // standard TOTP window
    private static final int OTP_DIGITS = 6;

    // Secret generation

    /**
     Generates a new cryptographically random Base32 encoded TOTP secret
     Store this in UserAccount.totpSecret and never expose it after sign up
     */
    public static String generateSecret() {
        byte[] raw = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(raw);
        return base32Encode(raw);
    }

    // Code generation & verification

    /**
     Returns the current 6 digit TOTP code for the given secret
     Useful for testing, the real verification is done in verify()
     */
    public static String getCurrentCode(String secret) {
        long counter = System.currentTimeMillis() / 1000L / TIME_STEP_SECS;
        return computeHOTP(base32Decode(secret), counter);
    }

    /**
     Verifies a user supplied code against the secret
     
     @param secret the stored Base32 secret
     @param code the 6 digit string entered by the user
     @return true if the code is valid
     */

    public static boolean verify(String secret, String code) {
        if (code == null || code.length() != OTP_DIGITS) return false;
        byte[] key = base32Decode(secret);
        long counter = System.currentTimeMillis() / 1000L / TIME_STEP_SECS;
        for (int offset = -1; offset <= 1; offset++) {
            if (computeHOTP(key, counter + offset).equals(code)) return true;
        }
        return false;
    }

    // otpauth URI — used to build the QR code

    /**
     Returns the otpauth:// URI that authenticator apps scan.
     
     @param issuer your app name (e.g. "MI Encrypt")
     @param username the account username
     @param secret the Base32-encoded secret
     */
    public static String buildOtpauthUri(String issuer, String username, String secret) {
        try {
            String encIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8.name()).replace("+", "%20");
            String encUser = URLEncoder.encode(username, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return "otpauth://totp/" + encIssuer + ":" + encUser
                    + "?secret=" + secret
                    + "&issuer=" + encIssuer
                    + "&algorithm=SHA1&digits=6&period=30";
        } catch (Exception e) {
            // Fallback — plain concatenation
            return "otpauth://totp/" + issuer + ":" + username + "?secret=" + secret;
        }
    }

    /**
     Returns a URL that displays a 200×200 QR code PNG for otpauth URI, 
     from a free qrserver.com API (needs internet connection)
    */
    public static String buildQrImageUrl(String otpauthUri) {
        try {
            String encoded = URLEncoder.encode(otpauthUri, StandardCharsets.UTF_8.name());
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encoded;
        } catch (Exception e) {
            return "";
        }
    }

    // HOTP core

    private static String computeHOTP(byte[] key, long counter) {
        try {
            // Pack counter into 8-byte big end 
            byte[] msg = new byte[8];
            long c = counter;
            for (int i = 7; i >= 0; i--) {
                msg[i] = (byte) (c & 0xFF);
                c >>= 8;
            }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(msg);

            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                        | ((hash[offset + 1] & 0xFF) << 16)
                        | ((hash[offset + 2] & 0xFF) <<  8)
                        | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, OTP_DIGITS);
            return String.format("%0" + OTP_DIGITS + "d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP computation failed", e);
        }
    }

    // Base32 encode / decode 

    public static String base32Encode(byte[] data) {
        if (data.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int buffer = data[0];
        int bitsLeft = 8;
        int index = 1;

        while (bitsLeft > 0 || index < data.length) {
            if (bitsLeft < 5) {
                if (index < data.length) {
                    buffer <<= 8;
                    buffer |= (data[index++] & 0xFF);
                    bitsLeft += 8;
                } else {
                    buffer <<= (5 - bitsLeft);
                    bitsLeft = 5;
                }
            }
            int charIndex = (buffer >> (bitsLeft - 5)) & 0x1F;
            bitsLeft -= 5;
            sb.append(BASE32_ALPHABET.charAt(charIndex));
        }
        return sb.toString(); // no '=' padding — authenticators accept it
    }

    public static byte[] base32Decode(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("[=\\s]", "");
        int outputLen = (encoded.length() * 5) / 8;
        byte[] result = new byte[outputLen];
        int buffer = 0;
        int bitsLeft = 0;
        int idx = 0;

        for (char c : encoded.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) continue; 
            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[idx++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        return result;
    }
}
