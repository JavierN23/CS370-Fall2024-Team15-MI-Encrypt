public class PasswordStrengthChecker {

    public static int getStrengthScore(String password) {
        if (password == null) return 0;

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;

        return score;
    }

    public static String getStrengthLabel(String password) {
        int score = getStrengthScore(password);

        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }

    public static boolean isWeak(String password) {
        return getStrengthScore(password) <= 2;
    }
}