package com.cdw.cdw.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"language_id", "message_key"})
})
@Data
public class Translation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "message_key", nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;
}
