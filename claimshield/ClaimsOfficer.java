package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Represents a claims officer in the ClaimShield system responsible for
 * reviewing and processing claims.
 */
public class ClaimsOfficer extends User {

    /**
     * Constructs a new ClaimsOfficer user.
     *
     * @param userId   the unique user identifier (u-7digits)
     * @param username the username for login
     * @param password the password for authentication
     * @param fullName the full name of the claims officer
     * @param email    the email address of the claims officer
     * @param status   the account status (ACTIVE/INACTIVE)
     */
    public ClaimsOfficer(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status) {
        super(userId, username, password, fullName, email, UserRole.OFFICER, status);
    }

    /**
     * Displays the Claims Officer dashboard banner and role information.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=========================================");
        System.out.println("  CLAIMS OFFICER DASHBOARD - CLAIMSHIELD ");
        System.out.println("=========================================");
        System.out.println("User ID     : " + getUserId());
        System.out.println("Officer Name: " + getFullName());
        System.out.println("Email       : " + getEmail());
        System.out.println("Role        : " + getRole());
        System.out.println("Status      : " + getStatus());
        System.out.println("Privileges  : Claim Review & Approval");
        System.out.println("=========================================");
    }

    /**
     * Formats the claims officer record into an 8-column CSV string for users.txt persistence.
     * Format: userId,username,password,fullName,email,role,status,customerId (blank)
     *
     * @return the CSV representation of the claims officer user for users.txt
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
        return "ClaimsOfficer{userId=" + getUserId()
                + ", username=" + getUsername()
                + ", fullName=" + getFullName()
                + ", email=" + getEmail()
                + ", status=" + getStatus()
                + "}";
    }
}
