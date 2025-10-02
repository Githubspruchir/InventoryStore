package com.ruchir.InventoryStore.Service;

import com.ruchir.InventoryStore.model.Product;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product createProductWithImage(Product product, MultipartFile file);
    Product getProduct(Long id);
    List<Product> getAllProducts();
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    Product increaseStock(Long id, int qty);
    Product decreaseStock(Long id, int qty);
    List<Product> getLowStockProducts();
}
