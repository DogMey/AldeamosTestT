package com.example.producer.repository;

import com.example.producer.entity.OriginLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginLineRepository extends JpaRepository<OriginLine, Long> {

    boolean existsByPhoneNumberAndActiveTrue(String phoneNumber);
}
