package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarnRepository extends JpaRepository<Warn, Long> {

    @EntityGraph(attributePaths = "trigger")
    Page<Warn> findAllByMediaIdAndMediaType(Long id, MediaType mediaType, Pageable pageable);
    Page<Warn> findAllByUserEmail(String userEmail, Pageable pageable);
    boolean existsByUserIdAndTriggerIdAndMediaIdAndMediaType(Long userId, Long triggerId, Long mediaId, MediaType mediaType);
    Optional<Warn> findByIdAndUserEmail(Long id, String userEmail);
    boolean existsByIdAndUserEmail(Long id, String userEmail);

}
