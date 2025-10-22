package intregrated.backend.repositories.orders;

import intregrated.backend.entities.orders.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findByUser_Id(Integer userId, Pageable pageable);
    Page<Order> findBySeller_Id(Integer sellerId, Pageable pageable);
}
