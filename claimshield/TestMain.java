package claimshield;
import java.time.LocalDateTime;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */
public class TestMain {
    public static void main(String[] args) {
        ClaimManager manager = new ClaimManager();

        Customer holder = new Customer("c-1000001", "Nguyen Van A", "PolicyHolder", null);
        System.out.println("Add PolicyHolder: " + manager.addCustomer(holder));

        Customer dependent = new Customer("c-1000002", "Nguyen Van B", "Dependent", "c-1000001");
        System.out.println("Add Dependent: " + manager.addCustomer(dependent));

        System.out.println("Add duplicate ID: " + manager.addCustomer(holder));

        Customer badId = new Customer("c-123", "Bad Id", "PolicyHolder", null);
        System.out.println("Add bad ID: " + manager.addCustomer(badId));

        Customer orphanDependent = new Customer("c-1000003", "No Parent", "Dependent", "c-9999999");
        System.out.println("Add orphan dependent: " + manager.addCustomer(orphanDependent));

        InsuranceCard validCard = new InsuranceCard("1234567890", "c-1000001", "c-1000001", LocalDateTime.of(2027, 12, 31, 0, 0));
        System.out.println("Add valid card: " + manager.addInsuranceCard(validCard));

        InsuranceCard dupCard = new InsuranceCard("1234567890", "c-1000001", "c-1000001", LocalDateTime.of(2027, 12, 31, 0, 0));
        System.out.println("Add duplicate card number: " + manager.addInsuranceCard(dupCard));

        InsuranceCard badCardNumber = new InsuranceCard("123", "c-1000001", "c-1000001", LocalDateTime.of(2027, 12, 31, 0, 0));
        System.out.println("Add bad card number: " + manager.addInsuranceCard(badCardNumber));

        InsuranceCard ownerIsDependent = new InsuranceCard("2222222222", "c-1000001", "c-1000002", LocalDateTime.of(2027, 12, 31, 0, 0));
        System.out.println("Add card with Dependent as owner: " + manager.addInsuranceCard(ownerIsDependent));

        InsuranceCard holderNotExist = new InsuranceCard("3333333333", "c-9999999", "c-1000001", LocalDateTime.of(2027, 12, 31, 0, 0));
        System.out.println("Add card with nonexistent holder: " + manager.addInsuranceCard(holderNotExist));

        Claim validClaim = new Claim("f-1000000001", LocalDateTime.of(2026, 7, 1, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2026, 6, 30, 9, 0), 1500000, "New");
        System.out.println("Add valid claim: " + manager.addClaim(validClaim));

        Claim dupClaimId = new Claim("f-1000000001", LocalDateTime.of(2026, 7, 1, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2026, 6, 30, 9, 0), 1500000, "New");
        System.out.println("Add duplicate claim id: " + manager.addClaim(dupClaimId));

        Claim negativeAmount = new Claim("f-1000000002", LocalDateTime.of(2026, 7, 1, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2026, 6, 30, 9, 0), -500, "New");
        System.out.println("Add negative amount claim: " + manager.addClaim(negativeAmount));

        Claim examAfterClaim = new Claim("f-1000000003", LocalDateTime.of(2026, 6, 1, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2026, 6, 30, 9, 0), 1000000, "New");
        System.out.println("Add exam after claim date: " + manager.addClaim(examAfterClaim));

        Claim examAfterExpiry = new Claim("f-1000000004", LocalDateTime.of(2028, 1, 5, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2028, 1, 1, 9, 0), 1000000, "New");
        System.out.println("Add exam after card expiry: " + manager.addClaim(examAfterExpiry));

        Claim badStatus = new Claim("f-1000000005", LocalDateTime.of(2026, 7, 1, 10, 0), "c-1000001", "1234567890", LocalDateTime.of(2026, 6, 30, 9, 0), 1000000, "Pending");
        System.out.println("Add invalid status claim: " + manager.addClaim(badStatus));
        
        System.out.println("Update status New to Processing: " + manager.updateClaimStatus("f-1000000001", "Processing"));
        System.out.println("Update status Processing to New (backward): " + manager.updateClaimStatus("f-1000000001", "New"));
        System.out.println("Update status Processing to Done: " + manager.updateClaimStatus("f-1000000001", "Done"));
        System.out.println("Update status with invalid value: " + manager.updateClaimStatus("f-1000000001", "Cancelled"));

        System.out.println("Add valid document: " + manager.addDocumentToClaim("f-1000000001", "f-1000000001_1234567890_receipt.pdf"));
        System.out.println("Add document wrong prefix: " + manager.addDocumentToClaim("f-1000000001", "wrong_prefix.pdf"));
        System.out.println("Add document empty name part: " + manager.addDocumentToClaim("f-1000000001", "f-1000000001_1234567890_.pdf"));

        System.out.println("Get all customers count: " + manager.getAllCustomers().size());
        System.out.println("Get all cards count: " + manager.getAllCards().size());
        System.out.println("Get all claims count: " + manager.getAllClaims().size());

        System.out.println("Delete existing customer: " + manager.deleteCustomer("c-1000002"));
        System.out.println("Delete nonexistent customer: " + manager.deleteCustomer("c-9999999"));
        System.out.println("Get all customers count after delete: " + manager.getAllCustomers().size());
        

        System.out.println("\n===== Delete test =====");
        System.out.println("Delete existing card: " + manager.deleteInsuranceCard("1234567890"));
        System.out.println("Delete nonexistent card: " + manager.deleteInsuranceCard("9999999999"));
        System.out.println("Delete existing claim: " + manager.deleteClaim("f-1000000001"));
        System.out.println("Delete nonexistent claim: " + manager.deleteClaim("f-9999999999"));

        System.out.println("\n===== Defensive copy test =====");
        java.util.ArrayList<Customer> list = manager.getAllCustomers();
        list.clear();
        System.out.println("Manager customers after external clear(): " + manager.getAllCustomers().size());

        System.out.println("\n===== FILE SAVE TEST =====");
        manager.saveCustomersToFile("data/customers.txt");
        System.out.println("Saved customers to file. Check data/customers.txt");
        
        System.out.println("\n===== FILE LOAD TEST =====");
        ClaimManager loadedManager = new ClaimManager();
        loadedManager.loadCustomersFromFile("data/customers.txt");
        System.out.println("Loaded customers count: " + loadedManager.getAllCustomers().size());
        for (Customer c : loadedManager.getAllCustomers()) {
            System.out.println(c);
        }

        System.out.println("\n===== Card file save/load test =====");
        manager.saveCardsToFile("data/cards.txt");
        System.out.println("Saved cards to file. Check data/cards.txt");

        ClaimManager loadedCardManager = new ClaimManager();
        loadedCardManager.loadCardsFromFile("data/cards.txt");
        System.out.println("Loaded cards count: " + loadedCardManager.getAllCards().size());
        for (InsuranceCard c : loadedCardManager.getAllCards()) {
            System.out.println(c);
        }

        System.out.println("\n===== Claim file save/load test =====");
        manager.saveClaimsToFile("data/claims.txt");
        System.out.println("Saved claims to file. Check data/claims.txt");

        ClaimManager loadedClaimManager = new ClaimManager();
        loadedClaimManager.loadClaimsFromFile("data/claims.txt");
        System.out.println("Loaded claims count: " + loadedClaimManager.getAllClaims().size());
        for (Claim c : loadedClaimManager.getAllClaims()) {
            System.out.println(c);
        }
    }
}
