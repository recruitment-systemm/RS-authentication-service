package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.authenticationservice.entity.EmployeeRole;

public record CreateEmployeeRequest(

        @NotBlank
        String username,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotNull
        EmployeeRole role

) {
}