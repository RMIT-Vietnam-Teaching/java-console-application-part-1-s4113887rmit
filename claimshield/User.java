package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Abstract base class representing a user account in the ClaimShield system.
 */
public abstract class User {
    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus status;

    /**
     * Constructs a new User with all user account details.
     *
     * @param userId   the unique user identifier (u-7digits)
     * @param username the username for login
     * @param password the password for authentication
     * @param fullName the full name of the user
     * @param email    the email address of the user
     * @param role     the system role assigned to the user
     * @param status   the account status (ACTIVE/INACTIVE)
     */
    public User(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserRole role,
            UserStatus status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    /**
     * Gets the unique user identifier.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the user's password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the full name of the user.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the email address of the user.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the role assigned to the user.
     *
     * @return the UserRole
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Gets the current account status of the user.
     *
     * @return the UserStatus
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Sets the unique user identifier.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the full name.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Sets the email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user role.
     *
     * @param role the UserRole to set
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Sets the user account status.
     *
     * @param status the UserStatus to set
     */
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * Displays a role-specific dashboard banner/header for the user.
     */
    public abstract void displayDashboard();

    /**
     * Helper method to format common user fields for users.txt persistence.
     * Format: userId,username,password,fullName,email,role,status
     *
     * @return comma-separated user base fields
     */
    protected String toUserBaseFileString() {
        return userId + ","
                + username + ","
                + password + ","
                + fullName + ","
                + email + ","
                + role + ","
                + status;
    }

    /**
     * Formats the user record into an 8-column CSV string for users.txt persistence.
     * Format: userId,username,password,fullName,email,role,status,customerId
     * For non-customer users (Admin, ClaimsOfficer), customerId is blank.
     *
     * @return the CSV representation of the user for users.txt
     */
    public String toUserFileString() {
        return toUserBaseFileString() + ",";
    }

    /**
     * Formats the user record into a CSV string for file persistence.
     * Defaults to toUserFileString().
     *
     * @return the CSV representation of the user
     */
    public String toFileString() {
        return toUserFileString();
    }

    @Override
    public String toString() {
        return "User{userId=" + userId
                + ", username=" + username
                + ", fullName=" + fullName
                + ", email=" + email
                + ", role=" + role
                + ", status=" + status
                + "}";
    }
}
