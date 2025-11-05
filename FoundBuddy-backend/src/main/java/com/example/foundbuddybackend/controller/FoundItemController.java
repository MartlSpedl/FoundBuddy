package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.model.FoundItem;
import com.example.foundbuddybackend.repository.FoundItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * REST controller exposing CRUD endpoints for {@link FoundItem} entities.
 *
 * <p>
 * All endpoints are prefixed with {@code /api/found-items} and are CORS
 * enabled to allow cross‑origin requests from the Android front‑end.  The
 * controller delegates persistence to {@link FoundItemRepository}.  Note
 * that image handling is out of scope; the {@code imageUri} field is
 * preserved as supplied by clients.
 */
@RestController
@RequestMapping("/api/found-items")
@CrossOrigin(origins = "*")
public class FoundItemController {

    private final FoundItemRepository repository;

    public FoundItemController(FoundItemRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all found items sorted by descending creation time.
     *
     * @return list of all found items
     */
    @GetMapping
    public List<FoundItem> getAll() {
        List<FoundItem> items = repository.findAll();
        // sort descending by createdAt (nulls last)
        items.sort((a, b) -> {
            Long tsA = a.getCreatedAt();
            Long tsB = b.getCreatedAt();
            if (tsA == null && tsB == null) return 0;
            if (tsA == null) return 1;
            if (tsB == null) return -1;
            return tsB.compareTo(tsA);
        });
        return items;
    }

    /**
     * Retrieves a single found item by its identifier.
     *
     * @param id the item identifier
     * @return the found item or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoundItem> getById(@PathVariable Long id) {
        Optional<FoundItem> item = repository.findById(id);
        return item.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new found item.  The {@code id} property, if present in the
     * request, is ignored; the backend will assign its own value.  If
     * {@code createdAt} is not provided it will be set to the current time.
     *
     * @param item incoming item from the client
     * @return the persisted item with generated identifier
     */
    @PostMapping
    public ResponseEntity<FoundItem> create(@RequestBody FoundItem item) {
        item.setId(null);
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(Instant.now().toEpochMilli());
        }
        FoundItem saved = repository.save(item);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Deletes all found items from the repository.  Returns HTTP 204 on
     * success.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}