package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String username;
    String fullName;
    String phoneNumber;
    String address;
    boolean active;
    Set<Role> roles;
}
