package claimshield;

import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Shared console constants and input-validation helpers used by the focused
 * role and entity menu classes.
 */
final class ConsoleSupport {
    static final String USERS_FILE = "data/users.txt";
    static final String CUSTOMERS_FILE = "data/customers.txt";
    static final String CARDS_FILE = "data/cards.txt";
    static final String CLAIMS_FILE = "data/claims.txt";
    static final String LOGS_FILE = "data/logs.txt";

    private ConsoleSupport() {
    }

    /**
     * Reads one line of console input. EOF is handled by the entry point's
     * top-level shutdown block, so no raw stack trace escapes to the user.
     *
     * @param sc the active console scanner
     * @return the next input line
     */
    static String readLine(Scanner sc) {
        if (!sc.hasNextLine()) {
            throw new EndOfInputException();
        }
        return sc.nextLine();
    }

    /**
     * Validates the free-text fields shared by account-registration flows.
     *
     * @return null when valid, otherwise a description of the first violation
     */
    static String validateAccountFields(
            String username,
            String password,
            String fullName,
            String email
    ) {
        if (!Validator.isValidUsername(username)) {
            return "Username must be between " + Validator.USERNAME_MIN_LENGTH
                    + " and " + Validator.USERNAME_MAX_LENGTH + " characters long.";
        }
        if (!Validator.isValidPassword(password)) {
            return "Password must be between " + Validator.PASSWORD_MIN_LENGTH
                    + " and " + Validator.PASSWORD_MAX_LENGTH + " characters long.";
        }
        if (!Validator.isValidFullName(fullName)) {
            return "Full Name must be between " + Validator.NAME_MIN_LENGTH
                    + " and " + Validator.NAME_MAX_LENGTH + " characters long.";
        }
        if (!Validator.isValidEmail(email)) {
            return "Email must be between " + Validator.EMAIL_MIN_LENGTH
                    + " and " + Validator.EMAIL_MAX_LENGTH
                    + " characters long and contain an '@'.";
        }
        return null;
    }

    /** Signals that the console input stream has ended. */
    static final class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
