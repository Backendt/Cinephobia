package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.MediaType;
import fr.backendt.cinephobia.models.Warn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarnRepository extends JpaRepository<Warn, Long> {

    Page<Warn> findAllByMediaIdAndMediaType(Long id, MediaType mediaType, Pageable pageable);

}
