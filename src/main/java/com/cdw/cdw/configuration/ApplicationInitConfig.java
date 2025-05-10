package com.cdw.cdw.configuration;

import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.domain.enums.UserRole;
import com.cdw.cdw.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                var role = new HashSet<UserRole>();
                role.add(UserRole.ADMIN);
                User user = User.builder()
                        .username("admin")
                        .email("21130170@st.hcmuaf.edu.vn")
                        .fullName("Admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .roles(role)
                        .build();
                userRepository.save(user);
                log.warn("admin account hash generated");
            }
        };
    }


}
