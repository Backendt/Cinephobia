package fr.backendt.cinephobia.models.dto;

import lombok.*;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Generated
@NoArgsConstructor
public class ProfileResponseDTO extends UserResponseDTO {

    private Set<TriggerDTO> triggers;

    public ProfileResponseDTO(Long id, String displayName, String email, String password, String role, Set<TriggerDTO> triggers) {
        setId(id);
        setDisplayName(displayName);
        setEmail(email);
        setPassword(password);
        setRole(role);
        setTriggers(triggers);
    }

}
