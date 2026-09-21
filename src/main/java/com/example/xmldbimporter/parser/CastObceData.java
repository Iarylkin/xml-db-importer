package com.example.xmldbimporter.parser;

/**
 * The fields of a single {@code vf:CastObce} element that {@link XmlParser} extracts from the
 * XML: its own {@code kod}/{@code nazev}, plus {@code kodObce} - the {@code kod} of the parent
 * obec, read from the nested {@code vf:Obec/vf:Kod} reference.
 */
public record CastObceData(Long kod, String nazev, Long kodObce) {

    /**
     * @return true if {@code kod}, a non-blank {@code nazev}, and {@code kodObce} are all
     *     present - false for an unrelated/incomplete reference elsewhere in the document.
     */
    public boolean isComplete() {
        return kod != null && nazev != null && !nazev.isBlank() && kodObce != null;
    }
}
