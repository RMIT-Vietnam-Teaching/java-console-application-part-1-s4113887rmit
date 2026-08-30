package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents a medical claim submitted for reimbursement, including
 * the insured person, related card, claim dates, amount, status,
 * supporting documents, and processing officer ID.
 */
public class Claim {
    private String id;
    private LocalDateTime claimDate;
    private String insuredPersonId;
    private String cardNumber;
    private LocalDateTime examDate;
    private ArrayList<String> documents;
    private double claimAmount;
    private ClaimStatus status;
    private String processedByUserId;

    public Claim(
            String id,
            LocalDateTime claimDate,
            String insuredPersonId,
            String cardNumber,
            LocalDateTime examDate,
            double claimAmount,
            ClaimStatus status,
            String processedByUserId
    ) {
        this.id = id;
        this.claimDate = claimDate;
        this.insuredPersonId = insuredPersonId;
        this.cardNumber = cardNumber;
        this.examDate = examDate;
        this.documents = new ArrayList<>();
        this.claimAmount = claimAmount;
        this.status = status;
        this.processedByUserId = processedByUserId;
    }

    public Claim(
            String id,
            LocalDateTime claimDate,
            String insuredPersonId,
            String cardNumber,
            LocalDateTime examDate,
            double claimAmount,
            ClaimStatus status
    ) {
        this(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount, status, null);
    }

    public Claim(
            String id,
            LocalDateTime claimDate,
            String insuredPersonId,
            String cardNumber,
            LocalDateTime examDate,
            double claimAmount,
            String status
    ) {
        this(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount, ClaimStatus.fromString(status), null);
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

    public ClaimStatus getStatus() {
        return status;
    }

    public String getProcessedByUserId() {
        return processedByUserId;
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

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = ClaimStatus.fromString(status);
    }

    public void setProcessedByUserId(String processedByUserId) {
        this.processedByUserId = processedByUserId;
    }

    public String toFileString() {
        String documentsStr = String.join("|", documents);

        return id + ","
                + claimDate + ","
                + insuredPersonId + ","
                + cardNumber + ","
                + examDate + ","
                + claimAmount + ","
                + (status != null ? status.name() : "") + ","
                + documentsStr + ","
                + (processedByUserId != null ? processedByUserId : "");
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
                + ", processedByUserId=" + processedByUserId
                + "}";
    }
}
