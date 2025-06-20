package com.cdw.cdw.domain.entity;

import com.cdw.cdw.domain.enums.AuthProvider;
import com.cdw.cdw.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Builder (toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    String id;

    @Column(name = "username", length = 100, unique = true)
    String username;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    String passwordHash;

    @Column(name = "full_name", length = 255, nullable = false)
    String fullName;

    @Column(name = "phone_number", length = 20, unique = true)
    String phoneNumber;

    @Lob
    @Column(name = "address")
    String address;

    @ManyToMany
    Set<Role> roles;

    @Column(name= "code_active", nullable= false)
    String codeActive;

    @Column(name= "code_expired")
    Date codeExpired;

    @Column(name = "is_active", nullable = false)
    boolean active = false; // Default value

    @CreationTimestamp // Tự động gán thời gian tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp // Tự động gán thời gian cập nhật
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;


    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "image_url")
    private String imageUrl;

//    // Relationships (Inverse side) - Chỉ định rõ nếu cần FetchType
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//     Set<Order> ordersPlaced;
//
//    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
//     Set<Order> ordersHandled;
}