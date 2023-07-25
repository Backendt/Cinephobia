package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.Warn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarnRepository extends JpaRepository<Warn, Long> {

    List<Warn> findAllByMediaId(Long id);

}
