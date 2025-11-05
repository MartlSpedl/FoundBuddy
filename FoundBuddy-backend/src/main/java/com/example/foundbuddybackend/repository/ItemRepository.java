package com.example.foundbuddybackend.repository;

import com.example.foundbuddybackend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for {@link Item} entities.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds all items whose title or description contains the given substring
     * (case‑insensitive).  Spring Data JPA automatically derives the query
     * implementation based on the method name.
     *
     * @param title fragment of the title to search for
     * @param description fragment of the description to search for
     * @return a list of matching items
     */
    List<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}