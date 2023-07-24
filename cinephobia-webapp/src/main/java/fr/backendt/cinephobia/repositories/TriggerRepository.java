package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    Optional<Trigger> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
