package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findAllByTitleContainingIgnoreCase(String title);
    //boolean existsByTitleIgnoreCaseAndYearOfRelease(String title, Long yearOfRelease);
    boolean existsByTitleIgnoreCase(String title);
}
