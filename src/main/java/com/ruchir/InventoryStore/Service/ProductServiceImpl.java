package com.ruchir.InventoryStore.Service;


import com.ruchir.InventoryStore.dao.ProductRepository;
import com.ruchir.InventoryStore.Exceptions.InvalidStockOperationException;
import com.ruchir.InventoryStore.Exceptions.ResourceNotFoundException;
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
        Product existing = getProduct(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setLowStockThreshold(product.getLowStockThreshold());
        if (product.getStockQuantity() < 0) {
            throw new InvalidStockOperationException("Stock cannot be negative");
        }
        existing.setStockQuantity(product.getStockQuantity());
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
        return repo.save(product);
    }

    @Override
    public Product decreaseStock(Long id, int qty) {
        if (qty <= 0) {
            throw new InvalidStockOperationException("Quantity must be positive");
        }

        Product product = getProduct(id);
        int newQty = product.getStockQuantity() - qty;

        if (newQty < 0) {
            throw new InvalidStockOperationException("Insufficient stock available");
        }

        if (product.getLowStockThreshold() > 0 && newQty < product.getLowStockThreshold()) {
            throw new InvalidStockOperationException("Cannot decrease: would go below threshold of "
                    + product.getLowStockThreshold());
        }

        product.setStockQuantity(newQty);
        return repo.save(product);
    }



    @Override
    public List<Product> getLowStockProducts() {
        return repo.findByStockQuantityLessThan(10);
    }
}
