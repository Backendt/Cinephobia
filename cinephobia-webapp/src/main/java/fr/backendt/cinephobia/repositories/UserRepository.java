package fr.backendt.cinephobia.repositories;

import fr.backendt.cinephobia.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "triggers")
    Optional<User> findUserWithRelationsByEmail(String email);

    @Query("SELECT email FROM User u WHERE u.id = ?1")
    Optional<String> findEmailById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findAllByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    void deleteByEmailIgnoreCase(String email);
}
