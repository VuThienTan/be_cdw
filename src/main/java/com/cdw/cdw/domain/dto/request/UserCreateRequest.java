package com.cdw.cdw.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.NumberFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @NotBlank(message = "USERNAME_IS_REQUIRED")
    String username;

    @NotBlank(message = "PASSWORD_IS_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotBlank(message = "PASSWORD_IS_REQUIRED")
    @Size(min = 8, max = 20, message = "INVALID_PASSWORD")
    String password;

    String fullName;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "INVALID_PHONE_NUMBER")
    String phoneNumber;

    String address;
}
