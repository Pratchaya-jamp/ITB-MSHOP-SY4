package intregrated.backend.repositories;

import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.entities.carts.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser_Id(Integer userId);
    Optional<Cart> findByUser (UsersAccount user);
}
