package com.example.order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByOrderNumber(String orderNumber);
}