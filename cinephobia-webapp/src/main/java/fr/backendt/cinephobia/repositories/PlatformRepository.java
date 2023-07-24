package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByName(String name);
    Optional<Platform> findByNameContainingIgnoreCase(String name);
}
