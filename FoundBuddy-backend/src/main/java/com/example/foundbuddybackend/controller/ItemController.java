package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.model.Item;
import com.example.foundbuddybackend.model.ItemStatus;
import com.example.foundbuddybackend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing lost and found items.  Items are stored in
 * {@link ItemRepository} and can be retrieved, searched or created via
 * simple HTTP endpoints under {@code /api/items}.
 */
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemRepository repository;

    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all items sorted by descending timestamp.
     */
    @GetMapping
    public List<Item> getAll() {
        List<Item> items = repository.findAll();
        items.sort((a, b) -> {
            Long tsA = a.getTimestamp();
            Long tsB = b.getTimestamp();
            if (tsA == null && tsB == null) return 0;
            if (tsA == null) return 1;
            if (tsB == null) return -1;
            return tsB.compareTo(tsA);
        });
        return items;
    }

    /**
     * Retrieves a single item by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable Long id) {
        Optional<Item> item = repository.findById(id);
        return item.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new item.  The {@code id} field is ignored.  The timestamp
     * defaults to the current time if not specified.  Clients must supply
     * {@link ItemStatus} either as "FOUND" or "LOST".
     */
    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        item.setId(null);
        if (item.getTimestamp() == null) {
            item.setTimestamp(Instant.now().toEpochMilli());
        }
        Item saved = repository.save(item);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Searches items by title or description.  The query parameter is
     * case‑insensitive.  If the query is missing or blank, all items are
     * returned.
     */
    @GetMapping("/search")
    public List<Item> search(@RequestParam(name = "q", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }
        return repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }
}