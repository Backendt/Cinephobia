package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    List<Platform> findAllByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
