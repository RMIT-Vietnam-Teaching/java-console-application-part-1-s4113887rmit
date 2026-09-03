package claimshield;

import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Generic interface defining the standard CRUD contract shared by every
 * ClaimShield entity repository.
 * <p>
 * The contract deliberately narrows its failure mode to
 * {@link ClaimShieldException} rather than the overly broad
 * {@code Exception}. Implementations may narrow that clause further - for
 * example {@link ClaimRepository#add} declares only
 * {@link InvalidClaimDateException} - or omit it entirely when the operation
 * cannot fail on a business rule, as {@link CardRepository#add} does. This
 * keeps the checked-exception surface of each repository honest and avoids
 * forcing callers to write catch blocks for exceptions that can never be
 * thrown.
 *
 * @param <T> the type of entity managed
 */
public interface Manageable<T> {

    /**
     * Adds a new item to the repository after validation.
     *
     * @param item the entity to add
     * @return true if added successfully, false if basic validation fails
     * @throws ClaimShieldException if a business rule constraint is violated
     */
    boolean add(T item) throws ClaimShieldException;

    /**
     * Updates an existing item in the repository.
     *
     * @param item the entity with updated fields
     * @return true if updated successfully, false if not found or basic validation fails
     * @throws ClaimShieldException if a business rule constraint is violated
     */
    boolean update(T item) throws ClaimShieldException;

    /**
     * Deletes an item from the repository by its unique identifier.
     *
     * @param id the unique identifier of the item to remove
     * @return true if deleted successfully, false if not found
     */
    boolean delete(String id);

    /**
     * Retrieves an item by its unique identifier.
     *
     * @param id the unique identifier to look up
     * @return the matching entity, or null if not found
     */
    T getById(String id);

    /**
     * Retrieves a defensive copy of all items currently in the repository.
     *
     * @return a List containing all items
     */
    List<T> getAll();
}
