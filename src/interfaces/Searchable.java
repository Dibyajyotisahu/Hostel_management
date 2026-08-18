package interfaces;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic search contract implemented by DAO classes.
 * @param <T> the entity type this DAO manages
 */
public interface Searchable<T> {
    List<T> searchByKeyword(String keyword) throws SQLException;
    T findById(int id) throws SQLException;
}
