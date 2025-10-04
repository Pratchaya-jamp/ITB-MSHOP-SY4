package intregrated.backend.repositories;

import intregrated.backend.entities.orders.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findByBuyer_Id(Integer buyerId, Pageable pageable);
    Page<Order> findBySeller_Id(Integer sellerId, Pageable pageable);
}
