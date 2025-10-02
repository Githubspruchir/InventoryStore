package com.ruchir.InventoryStore.Controller;

import com.ruchir.InventoryStore.Exceptions.InvalidStockOperationException;
import com.ruchir.InventoryStore.model.Product;
import com.ruchir.InventoryStore.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(service.createProduct(product));
    }

    @PostMapping(value = "/with-image", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createWithImage(@RequestPart("product") Product product,
                                                   @RequestPart("image") MultipartFile file) {
        return ResponseEntity.ok(service.createProductWithImage(product, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(service.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/increase")
    public ResponseEntity<Product> increase(@PathVariable Long id, @RequestParam int qty) {
        return ResponseEntity.ok(service.increaseStock(id, qty));
    }

    @PostMapping("/{id}/decrease")
    public ResponseEntity<Product> decrease(@PathVariable Long id, @RequestParam int qty) {
        if (qty <= 0) throw new InvalidStockOperationException("Quantity must be positive");
        return ResponseEntity.ok(service.decreaseStock(id, qty));
    }


    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> lowStock() {
        return ResponseEntity.ok(service.getLowStockProducts());
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws Exception {
        Path filePath = Paths.get("uploads/images/").resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Image not found: " + filename);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
