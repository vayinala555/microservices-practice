package com.example.SagaOrchestratorService.repositioties;

import com.example.SagaOrchestratorService.entity.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaStateRepository extends JpaRepository<SagaState,Long> {
    Optional<SagaState> findByOrderId(String orderId);
}
