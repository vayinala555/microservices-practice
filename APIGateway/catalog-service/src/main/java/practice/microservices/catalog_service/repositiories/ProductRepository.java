package practice.microservices.catalog_service.repositiories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.microservices.catalog_service.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
