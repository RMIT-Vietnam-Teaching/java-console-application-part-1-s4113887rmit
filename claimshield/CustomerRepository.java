package claimshield;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Repository responsible for customer entity management, CRUD operations,
 * search filtering, and persistence to customers.txt (4-column business data).
 */
public class CustomerRepository implements CustomerManageable {
    private ArrayList<Customer> customers;

    public CustomerRepository() {
        this.customers = new ArrayList<>();
    }

    @Override
    public boolean add(Customer customer) {
        if (customer == null) {
            return false;
        }
        if (!Validator.isValidCustomerId(customer.getId())) {
            return false;
        }
        if (getById(customer.getId()) != null) {
            return false;
        }
        if (!Validator.isValidCustomerType(customer.getCustomerType())) {
            return false;
        }
        if (customer.getCustomerType().equals("PolicyHolder") && customer.getParentPolicyHolderId() != null) {
            return false;
        }
        if (customer.getCustomerType().equals("Dependent")) {
            Customer parent = getById(customer.getParentPolicyHolderId());
            if (parent == null || !Validator.isPolicyHolder(parent)) {
                return false;
            }
        }
        customers.add(customer);
        AuditLogger.log(AppContext.getCurrentActorId(), "CREATE_CUSTOMER", customer.getId());
        return true;
    }

    @Override
    public boolean update(Customer customer) {
        if (customer == null) {
            return false;
        }
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equals(customer.getId())) {
                customers.set(i, customer);
                AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_CUSTOMER", customer.getId());
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
        Customer target = getById(id);
        if (target != null) {
            if (target instanceof PolicyHolder && !((PolicyHolder) target).getDependents().isEmpty()) {
                System.out.println("Cannot delete customer " + id + ": Customer is a PolicyHolder with active dependents.");
                return false;
            }
            if (target.getInsuranceCard() != null) {
                System.out.println("Cannot delete customer " + id + ": Customer has an active insurance card linked.");
                return false;
            }
            customers.remove(target);
            AuditLogger.log(AppContext.getCurrentActorId(), "DELETE_CUSTOMER", id);
            return true;
        }
        return false;
    }

    @Override
    public Customer getById(String id) {
        if (id == null) {
            return null;
        }
        for (Customer c : customers) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<Customer> getAll() {
        return new ArrayList<>(customers);
    }

    /**
     * Filters customers by their position in the Customer hierarchy.
     * Matching is case-insensitive on the values returned by
     * {@link Customer#getCustomerType()}.
     *
     * @param customerType "PolicyHolder" or "Dependent"
     * @return a List of matching customers, empty if the type is unknown
     */
    @Override
    public List<Customer> filterByType(String customerType) {
        List<Customer> result = new ArrayList<>();
        if (customerType == null) {
            return result;
        }
        for (Customer c : customers) {
            if (c.getCustomerType().equalsIgnoreCase(customerType.trim())) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Filters customers down to the dependents covered under a given
     * PolicyHolder, resolved through the polymorphic
     * {@link Customer#getParentPolicyHolderId()} accessor.
     *
     * @param policyHolderId the customer ID of the covering PolicyHolder
     * @return a List of dependents linked to that PolicyHolder
     */
    @Override
    public List<Customer> filterByParentPolicyHolder(String policyHolderId) {
        List<Customer> result = new ArrayList<>();
        if (policyHolderId == null) {
            return result;
        }
        for (Customer c : customers) {
            if (policyHolderId.equals(c.getParentPolicyHolderId())) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Performs a case-insensitive partial-match search on customer full names.
     *
     * @param keyword the name fragment to search for
     * @return a List of customers whose full name contains the keyword
     */
    @Override
    public List<Customer> searchByName(String keyword) {
        List<Customer> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }
        String needle = keyword.trim().toLowerCase();
        for (Customer c : customers) {
            if (c.getFullName() != null && c.getFullName().toLowerCase().contains(needle)) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Loads customer business data from customers.txt, performing the Two-Pass join
     * against UserRepository to obtain the real login credentials for each
     * customer.
     *
     * @param filePath the path to customers.txt
     * @param userRepo the UserRepository containing loaded users.txt records
     */
    public void loadCustomersFromFile(String filePath, UserRepository userRepo) {
        customers.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Skipping invalid customer line: " + line);
                    continue;
                }

                try {
                    String customerId = parts[0].trim();
                    String fullName = parts[1].trim();
                    String customerType = parts[2].trim();
                    String parentId = parts[3].trim().equals("null") ? null : parts[3].trim();

                    // Two-Pass Join: Look up login credentials from UserRepository by customerId
                    User user = (userRepo != null) ? userRepo.getUserByCustomerId(customerId) : null;
                    String userId = (user != null) ? user.getUserId() : null;
                    String username = (user != null) ? user.getUsername() : null;
                    String password = (user != null) ? user.getPassword() : null;
                    String email = (user != null) ? user.getEmail() : null;
                    UserStatus status = (user != null) ? user.getStatus() : UserStatus.ACTIVE;

                    Customer customer = null;
                    if ("PolicyHolder".equalsIgnoreCase(customerType)) {
                        customer = new PolicyHolder(userId, username, password, fullName, email, status, customerId);
                    } else if ("Dependent".equalsIgnoreCase(customerType)) {
                        customer = new Dependent(userId, username, password, fullName, email, status, customerId,
                                parentId);
                    }

                    if (customer == null || !Validator.isValidCustomerId(customer.getId()) || getById(customer.getId()) != null) {
                        System.out.println("Skipping invalid customer line: " + line);
                    } else {
                        customers.add(customer);
                        if (userRepo != null) {
                            // Register the complete Customer object back into UserRepository
                            userRepo.registerCustomerUser(customer);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Skipping invalid customer line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }

    /**
     * Loads customers without an external user repository (fallback/standalone
     * mode).
     *
     * @param filePath the path to customers.txt
     */
    public void loadCustomersFromFile(String filePath) {
        loadCustomersFromFile(filePath, null);
    }

    /**
     * Saves all customers to the specified file in 4-column CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveCustomersToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Customer customer : customers) {
                writer.println(customer.toCustomerFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }
}
