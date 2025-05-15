package com.cdw.cdw.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleCreateRequest {
    @NotBlank(message = "NAME_IS_REQUIRED")
    String name;

    @NotBlank(message = "DESCRIPTION_IS_REQUIRED")
    String description;

    @NotEmpty(message = "PERMISSION_IS_REQUIRED")
    Set<String> permissions;
}
