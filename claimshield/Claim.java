package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents a medical claim submitted for reimbursement, including
 * the insured person, related card, claim dates, amount, status,
 * and supporting documents.
 */
public class Claim {
    private String id;
    private LocalDateTime claimDate;
    private String insuredPersonId;
    private String cardNumber;
    private LocalDateTime examDate;
    private ArrayList<String> documents;
    private double claimAmount;
    private String status;

    public Claim(
            String id,
            LocalDateTime claimDate,
            String insuredPersonId,
            String cardNumber,
            LocalDateTime examDate,
            double claimAmount,
            String status
    ) {
        this.id = id;
        this.claimDate = claimDate;
        this.insuredPersonId = insuredPersonId;
        this.cardNumber = cardNumber;
        this.examDate = examDate;
        this.documents = new ArrayList<>();
        this.claimAmount = claimAmount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getClaimDate() {
        return claimDate;
    }

    public String getInsuredPersonId() {
        return insuredPersonId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public ArrayList<String> getDocuments() {
        return new ArrayList<>(documents);
    }

    public double getClaimAmount() {
        return claimAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setClaimDate(LocalDateTime claimDate) {
        this.claimDate = claimDate;
    }

    public void setInsuredPersonId(String insuredPersonId) {
        this.insuredPersonId = insuredPersonId;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExamDate(LocalDateTime examDate) {
        this.examDate = examDate;
    }

    public void addDocument(String document) {
        documents.add(document);
    }

    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toFileString() {
        String documentsStr = String.join("|", documents);

        return id + ","
                + claimDate + ","
                + insuredPersonId + ","
                + cardNumber + ","
                + examDate + ","
                + claimAmount + ","
                + status + ","
                + documentsStr;
    }

    @Override
    public String toString() {
        return "Claim{id=" + id
                + ", claimDate=" + claimDate
                + ", insuredPersonId=" + insuredPersonId
                + ", cardNumber=" + cardNumber
                + ", examDate=" + examDate
                + ", documents=" + documents
                + ", claimAmount=" + claimAmount
                + ", status=" + status
                + "}";
    }
}
