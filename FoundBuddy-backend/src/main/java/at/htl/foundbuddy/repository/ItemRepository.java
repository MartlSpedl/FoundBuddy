package at.htl.foundbuddy.repository;

import at.htl.foundbuddy.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatusIgnoreCase(String status);
    List<Item> findByCategoryIgnoreCase(String category);
}
