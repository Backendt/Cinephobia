package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    private String email;

    private String password;

    private String role;

    @ManyToMany
    @JoinTable(inverseJoinColumns = @JoinColumn(name = "trigger_id"))
    private Set<Trigger> triggers;

    public User(String displayName, String email, String password, String role) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(Long id, String displayName, String email, String password, String role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(User user) {
        this.id = user.id;
        this.displayName = user.displayName;
        this.email = user.email;
        this.password = user.password;
        this.role = user.role;
        this.triggers = user.triggers != null ? new HashSet<>(user.triggers) : null;
    }
}
