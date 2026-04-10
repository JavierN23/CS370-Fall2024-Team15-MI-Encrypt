import java.util.Random;

public class PasswordUtils {

    // Shuffle Character Array
    public static void shuffleArray(char[] array) {
        Random rand = new Random();

        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
           
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // Generates a random password with uppercase, lowercase, digits, and special characters
    public static String generatePassword() {

    // Character sets
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+<>?";

        // Combine all character sets
        String allChars = upper + lower + digits + special;
        Random rand = new Random();

        // Create a password array of length 16
        char[] password = new char[16];

        // Ensure at least one character from each category
        password[0] = upper.charAt(rand.nextInt(upper.length()));
        password[1] = lower.charAt(rand.nextInt(lower.length()));
        password[2] = digits.charAt(rand.nextInt(digits.length()));
        password[3] = special.charAt(rand.nextInt(special.length()));


        for (int i = 4; i < 16; i++) {
            password[i] = allChars.charAt(rand.nextInt(allChars.length()));
        }
        shuffleArray(password);

        return new String(password);
    }
    
}
