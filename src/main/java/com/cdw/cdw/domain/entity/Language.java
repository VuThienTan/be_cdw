package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "languages")
@Data
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false, unique = true)
    private String code;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "is_default")
    private boolean isDefault = false;
}