package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
    boolean existsByTitleIgnoreCase(String title);
}
