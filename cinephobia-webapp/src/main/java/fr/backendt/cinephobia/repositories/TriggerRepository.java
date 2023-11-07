package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    Page<Trigger> findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
}
