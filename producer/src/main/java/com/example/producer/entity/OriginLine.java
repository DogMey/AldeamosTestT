package com.example.producer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "origin_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OriginLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
