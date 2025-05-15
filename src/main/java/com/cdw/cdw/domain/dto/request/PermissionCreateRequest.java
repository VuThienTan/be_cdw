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
public class PermissionCreateRequest {
    @NotBlank(message = "NAME_IS_REQUIRED")
    String name;

    @NotBlank(message = "DESCRIPTION_IS_REQUIRED")
    String description;
}
