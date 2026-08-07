package claimshield;

/**
 * Represents an insurance card issued to a customer, including
 * the card holder, policy owner, and expiration date.
 *
 * @author Nguyen Ngoc Quang Dang - S4113887
 */
import java.time.LocalDateTime;
public class InsuranceCard {
    private String cardNumber;
    private String cardHolderId;
    private String policyOwnerId;
    private LocalDateTime expirationDate;

    public InsuranceCard(String cardNumber,String cardHolderId,String policyOwnerId,LocalDateTime expirationDate) {
        this.cardNumber = cardNumber;
        this.cardHolderId = cardHolderId;
        this.policyOwnerId = policyOwnerId;
        this.expirationDate = expirationDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public String getCardHolderId() {
        return cardHolderId;
    }
    public String getPolicyOwnerId() {
        return policyOwnerId;
    }
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setCardHolderId(String cardHolderId) {
        this.cardHolderId = cardHolderId;
    }
    public void setPolicyOwnerId(String policyOwnerId) {
        this.policyOwnerId = policyOwnerId;
    }
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String toFileString() {
        return cardNumber + "," + cardHolderId + "," + policyOwnerId + "," + expirationDate;
    }

    @Override
    public String toString() {
        return "InsuranceCard{cardNumber=" + cardNumber + ", cardHolderId=" + cardHolderId + ", policyOwnerId=" + policyOwnerId + ", expirationDate=" + expirationDate + "}";
    }

}
