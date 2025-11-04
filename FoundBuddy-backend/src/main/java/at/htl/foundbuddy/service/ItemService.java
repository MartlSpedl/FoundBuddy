package at.htl.foundbuddy.service;

import at.htl.foundbuddy.model.Item;
import at.htl.foundbuddy.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemService {
    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public List<Item> all() { return repo.findAll(); }
    public Item create(Item i) { return repo.save(i); }
    public Item get(Long id) { return repo.findById(id).orElse(null); }
    public void delete(Long id) { repo.deleteById(id); }

    public List<Item> byStatus(String status) { return repo.findByStatusIgnoreCase(status); }
    public List<Item> byCategory(String category) { return repo.findByCategoryIgnoreCase(category); }
}
