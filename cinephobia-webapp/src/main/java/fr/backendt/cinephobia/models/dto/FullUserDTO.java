package fr.backendt.cinephobia.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class FullUserDTO {

    private Long id;

    private String displayName;
    private String email;
    private String password;
    private String role;

    @JsonIgnore // Not receivable, only sent to view
    private Set<TriggerDTO> triggers = new HashSet<>();

    public FullUserDTO(Long id, String displayName, String email, String password, String role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}