package fr.backendt.cinephobia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public User(String displayName, String email, String password, String role) {
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
    }
}
