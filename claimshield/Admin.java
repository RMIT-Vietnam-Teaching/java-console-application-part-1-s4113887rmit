package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Represents an administrator in the ClaimShield system with full system
 * privileges.
 */
public class Admin extends User {

    /**
     * Constructs a new Admin user.
     *
     * @param userId   the unique user identifier (u-7digits)
     * @param username the username for login
     * @param password the password for authentication
     * @param fullName the full name of the administrator
     * @param email    the email address of the administrator
     * @param status   the account status (ACTIVE/INACTIVE)
     */
    public Admin(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status) {
        super(userId, username, password, fullName, email, UserRole.ADMIN, status);
    }

    /**
     * Displays the Admin dashboard banner and role information.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=========================================");
        System.out.println("       ADMIN DASHBOARD - CLAIMSHIELD     ");
        System.out.println("=========================================");
        System.out.println("User ID   : " + getUserId());
        System.out.println("Admin Name: " + getFullName());
        System.out.println("Email     : " + getEmail());
        System.out.println("Role      : " + getRole());
        System.out.println("Status    : " + getStatus());
        System.out.println("Privileges: Full System Administration");
        System.out.println("=========================================");
    }

    /**
     * Formats the admin record into an 8-column CSV string for users.txt persistence.
     * Format: userId,username,password,fullName,email,role,status,customerId (blank)
     *
     * @return the CSV representation of the admin user for users.txt
     */
    @Override
    public String toUserFileString() {
        return toUserBaseFileString() + ",";
    }

    @Override
    public String toFileString() {
        return toUserFileString();
    }

    @Override
    public String toString() {
        return "Admin{userId=" + getUserId()
                + ", username=" + getUsername()
                + ", fullName=" + getFullName()
                + ", email=" + getEmail()
                + ", status=" + getStatus()
                + "}";
    }
}
