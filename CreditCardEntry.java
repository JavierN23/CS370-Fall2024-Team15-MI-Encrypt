import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreditCardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;
    private String cardholderName;
    private String encryptedCardNumber;
    private String expiryDate;
    private String encryptedCVV;
    private String cardType;
    private String dateAdded;

    public CreditCardEntry(String nickname, String cardholderName, String cardNumber,
                           String expiryDate, String cvv, String cardType) {
        this.nickname = nickname;
        this.cardholderName = cardholderName;
        this.encryptedCardNumber = EncryptionService.encryptCTR(cardNumber);
        this.expiryDate = expiryDate;
        this.encryptedCVV = EncryptionService.encryptCTR(cvv);
        this.cardType = cardType;
        this.dateAdded = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    // Getters

    public String getNickname()          { return nickname; }
    public String getCardholderName()    { return cardholderName; }
    public String getExpiryDate()        { return expiryDate; }
    public String getCardType()          { return cardType; }
    public String getDateAdded()         { return dateAdded; }

    public String getCardNumber() {
        return EncryptionService.decryptCTR(encryptedCardNumber);
    }
    public String getEncryptedCardNumber() { return encryptedCardNumber; }

    public String getCVV() {
        return EncryptionService.decryptCTR(encryptedCVV);
    }
    public String getEncryptedCVV() { return encryptedCVV; }

    // Return last four digits, e.g. "**** **** **** 4242" 
    public String getMaskedCardNumber() {
        String num = getCardNumber();
        if (num == null || num.length() < 4) return "****";
        return "**** **** **** " + num.substring(num.length() - 4);
    }

    // Setters
    public void setNickname(String nickname)             { this.nickname = nickname; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public void setExpiryDate(String expiryDate)         { this.expiryDate = expiryDate; }
    public void setCardType(String cardType)             { this.cardType = cardType; }

    public void setCardNumber(String plain) {
        this.encryptedCardNumber = EncryptionService.encryptCTR(plain);
    }
    public void setCVV(String plain) {
        this.encryptedCVV = EncryptionService.encryptCTR(plain);
    }

    @Override
    public String toString() {
        return nickname + "  |  " + getMaskedCardNumber();
    }
}
