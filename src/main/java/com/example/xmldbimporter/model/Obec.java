package com.example.xmldbimporter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A municipality (obec), identified by its RÚIAN {@code kod} rather than a generated id.
 */
@Entity
@Table(name = "obec")
public class Obec {

    @Id
    private Long kod;

    @Column(nullable = false)
    private String nazev;

    protected Obec() {
    }

    public Obec(Long kod, String nazev) {
        this.kod = kod;
        this.nazev = nazev;
    }

    public Long getKod() {
        return kod;
    }

    public String getNazev() {
        return nazev;
    }
}
