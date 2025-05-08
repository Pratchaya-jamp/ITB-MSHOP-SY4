package intregrated.backend.repositories;

import intregrated.backend.entities.BrandBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandBaseRepo extends JpaRepository<BrandBase, Integer> {
    Optional<BrandBase> findByNameIgnoreCase(String name);
}
