package com.example.xmldbimporter.parser;

public record ObecData(Long kod, String nazev) {

    public boolean isComplete() {
        return kod != null && nazev != null && !nazev.isBlank();
    }
}
