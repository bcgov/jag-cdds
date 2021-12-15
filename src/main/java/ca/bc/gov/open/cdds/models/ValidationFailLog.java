package ca.bc.gov.open.cdds.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ValidationFailLog {
    private String message;
}
