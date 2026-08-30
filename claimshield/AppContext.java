package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Central application context and data orchestration container that owns
 * all four focused repositories (User, Customer, Card, Claim) and executes
 * the Two-Pass Wiring data loading lifecycle.
 */
public class AppContext {
    private UserRepository userRepository;
    private CustomerRepository customerRepository;
    private CardRepository cardRepository;
    private ClaimRepository claimRepository;
    private static User currentSessionUser;

    /**
     * Initializes the AppContext with fresh instances of all four repositories
     * and establishes inter-repository references.
     */
    public AppContext() {
        this.userRepository = new UserRepository();
        this.customerRepository = new CustomerRepository();
        this.cardRepository = new CardRepository();
        this.claimRepository = new ClaimRepository(customerRepository, cardRepository);
    }

    public static void setCurrentSessionUser(User user) {
        currentSessionUser = user;
    }

    public static User getCurrentSessionUser() {
        return currentSessionUser;
    }

    public static String getCurrentActorId() {
        return (currentSessionUser != null && currentSessionUser.getUserId() != null)
                ? currentSessionUser.getUserId()
                : "SYSTEM";
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public CardRepository getCardRepository() {
        return cardRepository;
    }

    public ClaimRepository getClaimRepository() {
        return claimRepository;
    }

    /**
     * Executes the complete Two-Pass Wiring data loading lifecycle in sequential
     * order:
     * 1. Pass 1a: Load users.txt into UserRepository
     * 2. Pass 1b: Load customers.txt into CustomerRepository, joining credentials
     * from UserRepository by customerId
     * 3. Pass 1c: Load cards.txt into CardRepository
     * 4. Pass 2: Wire domain object relationships (link cards to customers, link
     * dependents to policyholders)
     * 5. Pass 3: Load claims.txt into ClaimRepository and accumulate total claim
     * amounts
     *
     * @param usersPath     file path to users.txt
     * @param customersPath file path to customers.txt
     * @param cardsPath     file path to cards.txt
     * @param claimsPath    file path to claims.txt
     */
    public void loadAll(String usersPath, String customersPath, String cardsPath, String claimsPath) {
        // 1. Pass 1a: Load all user accounts from users.txt
        userRepository.loadUsersFromFile(usersPath);

        // 2. Pass 1b: Load customers.txt and join with UserRepository by customerId
        customerRepository.loadCustomersFromFile(customersPath, userRepository);

        // 3. Pass 1c: Load insurance cards from cards.txt
        cardRepository.loadCardsFromFile(cardsPath);

        // 4. Pass 2: Wire object relationships
        wireRelationships();

        // 5. Pass 3: Load claims from claims.txt
        claimRepository.loadClaimsFromFile(claimsPath);

        // 6. Update cumulative claim totals on each customer
        calculateCustomerClaimTotals();
    }

    /**
     * Performs the Second Pass wiring to link entity relationships in memory:
     * - Links each customer's InsuranceCard reference by matching cardHolderId
     * - Links each Dependent into its parent PolicyHolder's dependents collection
     */
    public void wireRelationships() {
        // Wire insurance cards to customers
        for (InsuranceCard card : cardRepository.getAll()) {
            Customer holder = customerRepository.getById(card.getCardHolderId());
            if (holder != null) {
                holder.setInsuranceCard(card);
            }
        }

        // Wire dependents into parent PolicyHolders
        for (Customer customer : customerRepository.getAll()) {
            if (customer instanceof Dependent) {
                Dependent dependent = (Dependent) customer;
                String parentId = dependent.getParentPolicyHolderId();
                if (parentId != null) {
                    Customer parent = customerRepository.getById(parentId);
                    if (parent instanceof PolicyHolder) {
                        ((PolicyHolder) parent).addDependent(dependent);
                    }
                }
            }
        }
    }

    /**
     * Calculates cumulative total approved claim amounts for all customers based on DONE claims.
     */
    public void calculateCustomerClaimTotals() {
        for (Claim claim : claimRepository.getAll()) {
            if (claim.getStatus() == ClaimStatus.DONE) {
                Customer customer = customerRepository.getById(claim.getInsuredPersonId());
                if (customer != null) {
                    customer.setTotalClaimAmount(customer.getTotalClaimAmount() + claim.getClaimAmount());
                }
            }
        }
    }

    /**
     * Atomically registers a new PolicyHolder across both CustomerRepository and UserRepository.
     * Performs comprehensive validation on user credentials, customer ID format, and duplicate checks.
     *
     * @param userId     unique user ID (u-7digits)
     * @param username   login username
     * @param password   login password
     * @param fullName   full name of the policyholder
     * @param email      email address
     * @param customerId unique customer ID (c-7digits)
     * @return true if registered successfully, false if validation fails or duplicate exists
     */
    public boolean registerPolicyHolder(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            String customerId) {
        if (!Validator.isValidUserId(userId) || !Validator.isValidCustomerId(customerId)) {
            return false;
        }
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            return false;
        }
        if (userRepository.getById(userId) != null
                || userRepository.getByUsername(username) != null
                || customerRepository.getById(customerId) != null
                || userRepository.getUserByCustomerId(customerId) != null) {
            return false;
        }

        PolicyHolder policyHolder = new PolicyHolder(
                userId,
                username.trim(),
                password.trim(),
                fullName.trim(),
                email.trim(),
                UserStatus.ACTIVE,
                customerId);

        if (!customerRepository.add(policyHolder)) {
            return false;
        }
        userRepository.registerCustomerUser(policyHolder);
        return true;
    }

    /**
     * Atomically registers a new Dependent across both CustomerRepository and UserRepository,
     * validating the parent PolicyHolder relationship and linking the dependent to the parent.
     *
     * @param userId               unique user ID (u-7digits)
     * @param username             login username
     * @param password             login password
     * @param fullName             full name of the dependent
     * @param email                email address
     * @param customerId           unique customer ID (c-7digits)
     * @param parentPolicyHolderId customer ID of the covering PolicyHolder
     * @return true if registered successfully, false if validation fails, duplicate exists, or parent not found
     */
    public boolean registerDependent(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            String customerId,
            String parentPolicyHolderId) {
        if (!Validator.isValidUserId(userId) || !Validator.isValidCustomerId(customerId)) {
            return false;
        }
        if (parentPolicyHolderId == null || !Validator.isValidCustomerId(parentPolicyHolderId)) {
            return false;
        }
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            return false;
        }
        if (userRepository.getById(userId) != null
                || userRepository.getByUsername(username) != null
                || customerRepository.getById(customerId) != null
                || userRepository.getUserByCustomerId(customerId) != null) {
            return false;
        }

        Customer parent = customerRepository.getById(parentPolicyHolderId);
        if (parent == null || !Validator.isPolicyHolder(parent)) {
            return false;
        }

        Dependent dependent = new Dependent(
                userId,
                username.trim(),
                password.trim(),
                fullName.trim(),
                email.trim(),
                UserStatus.ACTIVE,
                customerId,
                parentPolicyHolderId);

        if (!customerRepository.add(dependent)) {
            return false;
        }
        userRepository.registerCustomerUser(dependent);
        ((PolicyHolder) parent).addDependent(dependent);
        return true;
    }

    /**
     * Saves all repository datasets to their respective persistence files.
     *
     * @param usersPath     file path to users.txt
     * @param customersPath file path to customers.txt
     * @param cardsPath     file path to cards.txt
     * @param claimsPath    file path to claims.txt
     */
    public void saveAll(String usersPath, String customersPath, String cardsPath, String claimsPath) {
        userRepository.saveUsersToFile(usersPath);
        customerRepository.saveCustomersToFile(customersPath);
        cardRepository.saveCardsToFile(cardsPath);
        claimRepository.saveClaimsToFile(claimsPath);
    }
}
