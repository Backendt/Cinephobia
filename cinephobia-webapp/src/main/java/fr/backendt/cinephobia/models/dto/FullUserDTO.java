package fr.backendt.cinephobia.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

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
}