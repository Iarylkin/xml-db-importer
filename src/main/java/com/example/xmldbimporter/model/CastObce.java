package com.example.xmldbimporter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A part of a municipality (část obce), identified by its own {@code kod} and linked to the
 * {@link Obec} it belongs to via {@code kod_obce}.
 */
@Entity
@Table(name = "cast_obce")
public class CastObce {

    @Id
    private Long kod;

    @Column(nullable = false)
    private String nazev;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kod_obce", nullable = false)
    private Obec obec;

    protected CastObce() {
    }

    public CastObce(Long kod, String nazev, Obec obec) {
        this.kod = kod;
        this.nazev = nazev;
        this.obec = obec;
    }

    public Long getKod() {
        return kod;
    }

    public String getNazev() {
        return nazev;
    }

    public Obec getObec() {
        return obec;
    }
}
