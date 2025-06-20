package com.cdw.cdw.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {

    @NotBlank(message = "{username.required}")
    String username;

    @NotBlank(message = "{password.required}")
    @Size(min = 8, max = 20, message = "{invalid.password}")
    String password;

    @NotBlank(message = "{repassword.required}")
    private String repassword;

    @NotBlank(message = "{invalid.email}")
    @Email(message = "{invalid.email}")
    String email;

    String fullName;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "{invalid.phone.number}")
    String phoneNumber;

    String address;
}
