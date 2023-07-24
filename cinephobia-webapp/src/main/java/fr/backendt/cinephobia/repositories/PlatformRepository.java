package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Warn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends JpaRepository<Warn, Long> {
}
