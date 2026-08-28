package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.util.List;

/**
 * Generic interface defining standard CRUD operations for manageable entities.
 *
 * @param <T> the type of entity managed
 */
public interface Manageable<T> {

    /**
     * Adds a new item to the repository after validation.
     *
     * @param item the entity to add
     * @return true if added successfully, false if basic validation fails
     * @throws Exception if a business rule constraint is violated
     */
    boolean add(T item) throws Exception;

    /**
     * Updates an existing item in the repository.
     *
     * @param item the entity with updated fields
     * @return true if updated successfully, false if not found or basic validation fails
     * @throws Exception if a business rule constraint is violated
     */
    boolean update(T item) throws Exception;

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
