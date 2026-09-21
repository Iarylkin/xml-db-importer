package com.example.xmldbimporter.parser;

import java.util.List;

/**
 * The result of {@link XmlParser#parse}: the single obec found in the document (or {@code null}
 * if none was complete) and the cast obce records belonging to it.
 */
public record ParsedData(ObecData obec, List<CastObceData> castObceList) {
}
