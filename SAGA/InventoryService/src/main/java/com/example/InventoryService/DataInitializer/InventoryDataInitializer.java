package com.example.InventoryService.DataInitializer;

import com.example.InventoryService.entities.Inventory;
import com.example.InventoryService.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// this method initializes the data

@Component
public class InventoryDataInitializer implements ApplicationRunner {

    @Autowired
    InventoryRepository repo;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repo.count() == 0) {
            repo.save(new Inventory("SKU1", 100));
            repo.save(new Inventory("SKU2", 50));
            repo.save(new Inventory("SKU3", 200));
        }
    }
}
