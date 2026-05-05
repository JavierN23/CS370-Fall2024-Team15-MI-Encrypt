import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CreditCardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;
    private String cardholderName;
    private String encryptedCardNumber;
    private String expiryDate;
    private String encryptedCVV;
    private String cardType;
    private String dateAdded;
    private String businessGroup;

    public CreditCardEntry(String nickname, String cardholderName, String cardNumber,
                           String expiryDate, String cvv, String cardType) {
        this(nickname, cardholderName, cardNumber, expiryDate, cvv, cardType, null);
    }

    public CreditCardEntry(String nickname, String cardholderName, String cardNumber,
                           String expiryDate, String cvv, String cardType, String businessGroup) {
        this.nickname = nickname;
        this.cardholderName = cardholderName;
        this.encryptedCardNumber = EncryptionService.encryptCTR(cardNumber);
        this.expiryDate = expiryDate;
        this.encryptedCVV = EncryptionService.encryptCTR(cvv);
        this.cardType = cardType;
        this.businessGroup = businessGroup;
        this.dateAdded = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    // Getters

    public String getNickname()          { return nickname; }
    public String getCardholderName()    { return cardholderName; }
    public String getExpiryDate()        { return expiryDate; }
    public String getCardType()          { return cardType; }
    public String getDateAdded()         { return dateAdded; }
    public String getBusinessGroup()     { return businessGroup;}

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
    public void setBusinessGroup(String businessGroup)   { this.businessGroup = businessGroup; }

    public void setCardNumber(String plain) {
        this.encryptedCardNumber = EncryptionService.encryptCTR(plain);
    }
    public void setCVV(String plain) {
        this.encryptedCVV = EncryptionService.encryptCTR(plain);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreditCardEntry)) return false;
        CreditCardEntry that = (CreditCardEntry) o;
        return 
        Objects.equals(nickname, that.nickname) &&
        Objects.equals(cardholderName, that.cardholderName) &&
        Objects.equals(encryptedCardNumber, that.encryptedCardNumber) &&
        Objects.equals(expiryDate, that.expiryDate) &&
        Objects.equals(encryptedCVV, that.encryptedCVV) &&
        Objects.equals(cardType, that.cardType) &&
        Objects.equals(dateAdded, that.dateAdded) &&
        Objects.equals(businessGroup, that.businessGroup);
        
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, cardholderName, encryptedCardNumber, expiryDate, encryptedCVV, cardType, dateAdded, businessGroup);
    }

    @Override
    public String toString() {
        return nickname + "  |  " + getMaskedCardNumber();
    }
}