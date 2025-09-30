package intregrated.backend.repositories;

import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.entities.carts.CartItem;
import intregrated.backend.entities.saleitems.SaleItemBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepo extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartIdAndSaleItemAndSeller(Integer cartId, SaleItemBase saleItem, SellerAccount seller);
}
