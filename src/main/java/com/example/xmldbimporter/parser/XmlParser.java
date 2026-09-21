package com.example.xmldbimporter.parser;

import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming (StAX) parser for ČÚZK's RÚIAN exchange format (VFR).
 * Reads only what is needed to populate the DB:
 * - vf:Obec -> kod, nazev
 * - vf:CastObce -> kod, nazev, kod of the parent obec (nested vf:Obec/vf:Kod reference)
 *
 * <p>Only local element names are matched (not the namespace prefix), since
 * StAX exposes those independently of how the source XML declares "vf".
 * Any element occurrence that does not yield a complete record (see
 * {@link ObecData#isComplete()} / {@link CastObceData#isComplete()}) is
 * silently discarded, which also protects against unrelated nested
 * references to "Obec"/"CastObce" that other RÚIAN object types may contain.
 */
@Component
public class XmlParser {

    public ParsedData parse(InputStream xmlInputStream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        XMLStreamReader reader = factory.createXMLStreamReader(xmlInputStream);
        try {
            return doParse(reader);
        } finally {
            reader.close();
        }
    }

    private ParsedData doParse(XMLStreamReader reader) throws XMLStreamException {
        List<String> stack = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        ObecData obec = null;
        List<CastObceData> castObceList = new ArrayList<>();

        Long obecKod = null;
        String obecNazev = null;

        Long castObceKod = null;
        String castObceNazev = null;
        Long castObceKodObce = null;
        boolean insideCastObce = false;

        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String localName = reader.getLocalName();
                    if ("CastObce".equals(localName)) {
                        insideCastObce = true;
                        castObceKod = null;
                        castObceNazev = null;
                        castObceKodObce = null;
                    }
                    stack.add(localName);
                    text.setLength(0);
                }
                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> text.append(reader.getText());
                case XMLStreamConstants.END_ELEMENT -> {
                    String localName = stack.remove(stack.size() - 1);
                    String value = text.toString().trim();
                    text.setLength(0);
                    String parent = stack.isEmpty() ? null : stack.get(stack.size() - 1);

                    if ("Kod".equals(localName) && "Obec".equals(parent)) {
                        Long kod = parseLongOrNull(value);
                        if (insideCastObce) {
                            castObceKodObce = kod;
                        } else {
                            obecKod = kod;
                        }
                    } else if ("Nazev".equals(localName) && "Obec".equals(parent) && !insideCastObce) {
                        obecNazev = value;
                    } else if ("Kod".equals(localName) && "CastObce".equals(parent)) {
                        castObceKod = parseLongOrNull(value);
                    } else if ("Nazev".equals(localName) && "CastObce".equals(parent)) {
                        castObceNazev = value;
                    } else if ("Obec".equals(localName) && !insideCastObce) {
                        if (obec == null) {
                            ObecData candidate = new ObecData(obecKod, obecNazev);
                            if (candidate.isComplete()) {
                                obec = candidate;
                            }
                        }
                    } else if ("CastObce".equals(localName)) {
                        insideCastObce = false;
                        CastObceData candidate = new CastObceData(castObceKod, castObceNazev, castObceKodObce);
                        if (candidate.isComplete()) {
                            castObceList.add(candidate);
                        }
                    }
                }
                default -> {
                }
            }
        }

        return new ParsedData(obec, castObceList);
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
