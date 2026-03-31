package com.example.producer.repository;

import com.example.producer.entity.OriginLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OriginLineRepository extends JpaRepository<OriginLine, UUID> {

    boolean existsByOrigin(String origin);
}
