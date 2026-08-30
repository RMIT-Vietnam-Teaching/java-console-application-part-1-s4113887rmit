package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository responsible for user account management, authentication,
 * and user file persistence (users.txt).
 */
public class UserRepository implements UserManageable {
    private ArrayList<User> users;
    private Map<String, User> customerIdLookup;

    public UserRepository() {
        this.users = new ArrayList<>();
        this.customerIdLookup = new HashMap<>();
    }

    @Override
    public boolean add(User user) {
        if (user == null) {
            return false;
        }
        if (getById(user.getUserId()) != null) {
            return false;
        }
        if (getByUsername(user.getUsername()) != null) {
            return false;
        }
        users.add(user);
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            if (customer.getCustomerId() != null) {
                customerIdLookup.put(customer.getCustomerId(), customer);
            }
        }
        AuditLogger.log(AppContext.getCurrentActorId(), "CREATE_USER", user.getUserId());
        return true;
    }

    @Override
    public boolean update(User user) {
        if (user == null) {
            return false;
        }
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                if (user instanceof Customer) {
                    Customer c = (Customer) user;
                    if (c.getCustomerId() != null) {
                        customerIdLookup.put(c.getCustomerId(), c);
                    }
                }
                AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_USER", user.getUserId());
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
        User target = getById(id);
        if (target != null) {
            users.remove(target);
            if (target instanceof Customer) {
                customerIdLookup.remove(((Customer) target).getCustomerId());
            }
            AuditLogger.log(AppContext.getCurrentActorId(), "DELETE_USER", id);
            return true;
        }
        return false;
    }

    @Override
    public User getById(String id) {
        if (id == null) {
            return null;
        }
        for (User u : users) {
            if (u.getUserId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    /**
     * Finds a user by their unique login username (case-insensitive).
     *
     * @param username the username to search for
     * @return the matching User, or null if not found
     */
    public User getByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Retrieves a user by their linked customer ID.
     *
     * @param customerId the customer ID join key
     * @return the matching User/Customer, or null if not found
     */
    public User getUserByCustomerId(String customerId) {
        if (customerId == null) {
            return null;
        }
        return customerIdLookup.get(customerId);
    }

    /**
     * Registers or updates a Customer entity in both the users list and customer ID
     * lookup map.
     *
     * @param customer the Customer to register
     */
    public void registerCustomerUser(Customer customer) {
        if (customer == null) {
            return;
        }
        customerIdLookup.put(customer.getCustomerId(), customer);

        // Update in main list if already present by userId, otherwise add
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(customer.getUserId())) {
                users.set(i, customer);
                found = true;
                break;
            }
        }
        if (!found) {
            users.add(customer);
        }
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = getByUsername(username);
        if (user != null && user.getPassword().equals(password) && user.getStatus() == UserStatus.ACTIVE) {
            return user;
        }
        return null;
    }

    /**
     * Loads user accounts from users.txt.
     * Uses line.split(",", -1) to preserve trailing empty fields (for Admin/Officer
     * customerId column).
     *
     * @param filePath the path to users.txt
     */
    public void loadUsersFromFile(String filePath) {
        users.clear();
        customerIdLookup.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    System.out.println("Skipping invalid user line: " + line);
                    continue;
                }

                try {
                    String userId = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();
                    String fullName = parts[3].trim();
                    String email = parts[4].trim();
                    UserRole role = UserRole.valueOf(parts[5].trim().toUpperCase());
                    UserStatus status = UserStatus.valueOf(parts[6].trim().toUpperCase());
                    String customerId = (parts.length > 7 && !parts[7].trim().isEmpty()) ? parts[7].trim() : null;

                    User user = null;
                    if (role == UserRole.ADMIN) {
                        user = new Admin(userId, username, password, fullName, email, status);
                    } else if (role == UserRole.OFFICER) {
                        user = new ClaimsOfficer(userId, username, password, fullName, email, status);
                    } else if (role == UserRole.CUSTOMER) {
                        // Placeholder Customer until CustomerRepository wires the full domain model
                        // from customers.txt
                        user = new PolicyHolder(userId, username, password, fullName, email, status, customerId);
                    }

                    if (user != null) {
                        users.add(user);
                        if (customerId != null) {
                            customerIdLookup.put(customerId, user);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Skipping invalid user line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Saves all users to the specified file in 8-column CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveUsersToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User user : users) {
                writer.println(user.toUserFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}
