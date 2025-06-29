package practice.microservices.catalog_service.service;

import practice.microservices.catalog_service.entities.Product;

import java.util.List;

public interface ProductService {
    public List<Product> getAllProducts();
    public Product getProductById(long id);
    public Product createProduct(Product product);
    public Product updateProduct(long id, Product product);
    public void deleteProduct(long id);
}
