package intregrated.backend.repositories;

import intregrated.backend.entities.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
