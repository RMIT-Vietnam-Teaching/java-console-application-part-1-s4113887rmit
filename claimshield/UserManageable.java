package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Specialized interface extending Manageable for user account operations
 * and authentication.
 */
public interface UserManageable extends Manageable<User> {

    /**
     * Authenticates a user by username and password.
     *
     * @param username the login username
     * @param password the login password
     * @return the authenticated User if valid and active, or null otherwise
     */
    User authenticate(String username, String password);
}
