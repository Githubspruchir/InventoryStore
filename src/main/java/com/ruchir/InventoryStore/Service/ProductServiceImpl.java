package com.ruchir.InventoryStore.Service;

import com.ruchir.InventoryStore.Exceptions.InvalidStockOperationException;
import com.ruchir.InventoryStore.Exceptions.ResourceNotFoundException;
import com.ruchir.InventoryStore.dao.ProductRepository;
import com.ruchir.InventoryStore.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    @Override
    public Product createProduct(Product product) {
        validateStock(product);
        // Ensure id is not forced from Postman
        product.setId(null);
        return repo.save(product);
    }

    @Override
    public Product createProductWithImage(Product product, MultipartFile file) {
        try {
            String uploadDir = "uploads/images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            product.setImageUrl("/api/products/images/" + fileName);
            validateStock(product);
            product.setId(null); // avoid stale object
            return repo.save(product);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    @Override
    public Product getProduct(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = getProduct(id); // ensures it's managed entity

        if (product.getStockQuantity() < 0) {
            throw new InvalidStockOperationException("Stock cannot be negative");
        }
        if (product.getLowStockThreshold() > 0 &&
                product.getStockQuantity() < product.getLowStockThreshold()) {
            throw new InvalidStockOperationException(
                    "Stock quantity cannot be lower than the low-stock threshold (" + product.getLowStockThreshold() + ")"
            );
        }

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setLowStockThreshold(product.getLowStockThreshold());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setImageUrl(product.getImageUrl());

        return repo.save(existing);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public Product increaseStock(Long id, int qty) {
        Product product = getProduct(id);
        product.setStockQuantity(product.getStockQuantity() + qty);
        validateStock(product);
        return repo.save(product);
    }

    @Override
    public Product decreaseStock(Long id, int qty) {
        Product product = getProduct(id);
        if (product.getStockQuantity() < qty) {
            throw new InvalidStockOperationException("Insufficient stock available");
        }
        int newQty = product.getStockQuantity() - qty;
        if (newQty < product.getLowStockThreshold()) {
            throw new InvalidStockOperationException(
                    "Cannot decrease: stock would go below threshold (" + product.getLowStockThreshold() + ")"
            );
        }
        product.setStockQuantity(newQty);
        return repo.save(product);
    }

    @Override
    public List<Product> getLowStockProducts() {
        return repo.findByStockQuantityLessThan(10); // static threshold for now
    }

    // 🔹 Helper method for reusability
    private void validateStock(Product product) {
        if (product.getStockQuantity() < 0) {
            throw new InvalidStockOperationException("Stock cannot be negative");
        }
        if (product.getLowStockThreshold() > 0 &&
                product.getStockQuantity() < product.getLowStockThreshold()) {
            throw new InvalidStockOperationException(
                    "Stock quantity cannot be lower than the low-stock threshold (" + product.getLowStockThreshold() + ")"
            );
        }
    }
}
