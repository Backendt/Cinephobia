package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    List<Trigger> findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    boolean existsByNameIgnoreCase(String name);
}
