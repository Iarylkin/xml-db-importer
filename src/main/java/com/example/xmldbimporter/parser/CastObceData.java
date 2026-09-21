package com.example.xmldbimporter.parser;

public record CastObceData(Long kod, String nazev, Long kodObce) {

    public boolean isComplete() {
        return kod != null && nazev != null && !nazev.isBlank() && kodObce != null;
    }
}
