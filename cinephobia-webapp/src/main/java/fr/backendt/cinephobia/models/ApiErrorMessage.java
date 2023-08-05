package fr.backendt.cinephobia.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ApiErrorMessage {

    private HttpStatus status;

    private String message;

    private List<String> errors;

}
