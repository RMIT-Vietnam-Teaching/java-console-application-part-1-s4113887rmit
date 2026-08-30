package claimshield;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Repository responsible for insurance card entity management, CRUD operations,
 * and persistence to cards.txt.
 */
public class CardRepository implements Manageable<InsuranceCard> {
    private ArrayList<InsuranceCard> cards;

    public CardRepository() {
        this.cards = new ArrayList<>();
    }

    @Override
    public boolean add(InsuranceCard card) {
        if (card == null) {
            return false;
        }
        if (!Validator.isValidCardNumber(card.getCardNumber())) {
            return false;
        }
        if (getById(card.getCardNumber()) != null) {
            return false;
        }
        if (!Validator.isValidCustomerId(card.getCardHolderId())) {
            return false;
        }
        if (!Validator.isValidCustomerId(card.getPolicyOwnerId())) {
            return false;
        }
        cards.add(card);
        AuditLogger.log(AppContext.getCurrentActorId(), "CREATE_INSURANCE_CARD", card.getCardNumber());
        return true;
    }

    @Override
    public boolean update(InsuranceCard card) {
        if (card == null) {
            return false;
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getCardNumber().equals(card.getCardNumber())) {
                cards.set(i, card);
                AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_INSURANCE_CARD", card.getCardNumber());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        InsuranceCard target = getById(id);
        if (target != null) {
            cards.remove(target);
            AuditLogger.log(AppContext.getCurrentActorId(), "DELETE_INSURANCE_CARD", id);
            return true;
        }
        return false;
    }

    @Override
    public InsuranceCard getById(String id) {
        if (id == null) {
            return null;
        }
        for (InsuranceCard card : cards) {
            if (card.getCardNumber().equals(id)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Finds an insurance card associated with a specific card holder ID.
     *
     * @param cardHolderId the customer ID of the card holder
     * @return the matching InsuranceCard, or null if not found
     */
    public InsuranceCard getByCardHolderId(String cardHolderId) {
        if (cardHolderId == null) {
            return null;
        }
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(cardHolderId)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public List<InsuranceCard> getAll() {
        return new ArrayList<>(cards);
    }

    /**
     * Loads insurance cards from cards.txt.
     *
     * @param filePath the path to cards.txt
     */
    public void loadCardsFromFile(String filePath) {
        cards.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Skipping invalid card line: " + line);
                    continue;
                }

                try {
                    String cardNumber = parts[0].trim();
                    String cardHolderId = parts[1].trim();
                    String policyOwnerId = parts[2].trim();
                    LocalDateTime expirationDate = LocalDateTime.parse(parts[3].trim());

                    InsuranceCard card = new InsuranceCard(
                            cardNumber,
                            cardHolderId,
                            policyOwnerId,
                            expirationDate);

                    if (!Validator.isValidCardNumber(cardNumber)
                            || !Validator.isValidCustomerId(cardHolderId)
                            || !Validator.isValidCustomerId(policyOwnerId)
                            || getById(cardNumber) != null) {
                        System.out.println("Skipping invalid card line: " + line);
                    } else {
                        cards.add(card);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping invalid card line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading cards: " + e.getMessage());
        }
    }

    /**
     * Saves all insurance cards to the specified file in CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveCardsToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (InsuranceCard card : cards) {
                writer.println(card.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving cards: " + e.getMessage());
        }
    }
}
