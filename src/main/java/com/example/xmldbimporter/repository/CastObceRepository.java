package com.example.xmldbimporter.repository;

import com.example.xmldbimporter.model.CastObce;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD access to {@link CastObce}, keyed by its natural {@code kod}.
 */
public interface CastObceRepository extends JpaRepository<CastObce, Long> {
}
