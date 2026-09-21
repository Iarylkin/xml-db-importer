package com.example.xmldbimporter.repository;

import com.example.xmldbimporter.model.Obec;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD access to {@link Obec}, keyed by its natural {@code kod}.
 */
public interface ObecRepository extends JpaRepository<Obec, Long> {
}
