package com.example.foundbuddybackend.repository;

import com.example.foundbuddybackend.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link FoundItem} entities.
 */
@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
}