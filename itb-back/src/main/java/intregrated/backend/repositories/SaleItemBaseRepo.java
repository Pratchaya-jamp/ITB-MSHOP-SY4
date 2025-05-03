package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemBaseRepo extends JpaRepository<SaleItemBase, Integer> {
}
