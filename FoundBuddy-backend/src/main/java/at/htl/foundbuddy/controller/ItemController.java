package at.htl.foundbuddy.controller;

import at.htl.foundbuddy.model.Item;
import at.htl.foundbuddy.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<Item> all(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String category) {
        if (status != null) return service.byStatus(status);
        if (category != null) return service.byCategory(category);
        return service.all();
    }

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody Item item) {
        var saved = service.create(item);
        return ResponseEntity.created(URI.create("/api/items/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> get(@PathVariable Long id) {
        var found = service.get(id);
        return (found == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
