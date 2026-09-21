package com.example.xmldbimporter.parser;

/**
 * The fields of a single {@code vf:Obec} element that {@link XmlParser} extracts from the XML.
 */
public record ObecData(Long kod, String nazev) {

    /**
     * @return true if both {@code kod} and a non-blank {@code nazev} are present - false for
     *     a nested reference (e.g. inside {@code vf:CastObce}) that only carries a {@code kod}.
     */
    public boolean isComplete() {
        return kod != null && nazev != null && !nazev.isBlank();
    }
}
