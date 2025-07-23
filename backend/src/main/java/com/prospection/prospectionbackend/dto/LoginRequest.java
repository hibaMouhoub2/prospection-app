package com.prospection.prospectionbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email (message = "L'email doit être valide")
    private String email;

    @NotBlank (message = "Le mot de passe est obligatoire")
    private String motDePasse;
}
