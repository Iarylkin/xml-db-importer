package com.example.xmldbimporter.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming (StAX) parser for the source XML exchange format.
 * Reads only what is needed to populate the DB:
 * - vf:Obec -> kod, nazev
 * - vf:CastObce -> kod, nazev, kod of the parent obec (nested vf:Obec/vf:Kod reference)
 *
 * <p>Only local element names are matched (not the namespace prefix), since
 * StAX exposes those independently of how the source XML declares "vf".
 * Any element occurrence that does not yield a complete record (see
 * {@link ObecData#isComplete()} / {@link CastObceData#isComplete()}) is
 * silently discarded, which also protects against unrelated nested
 * references to "Obec"/"CastObce" that other object types in the source
 * format may contain.
 */
@Component
public class XmlParser {

    private static final Logger LOG = LoggerFactory.getLogger(XmlParser.class);

    /**
     * Streams through {@code xmlInputStream} once and extracts the obec/cast obce data. Does not
     * close {@code xmlInputStream} - that remains the caller's responsibility.
     *
     * @throws XMLStreamException if the input is not well-formed XML
     */
    public ParsedData parse(InputStream xmlInputStream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        XMLStreamReader reader = factory.createXMLStreamReader(xmlInputStream);
        try {
            ParsedData data = doParse(reader);
            LOG.debug("Parsed obec={} with {} cast obce record(s)",
                    data.obec() != null ? data.obec().kod() : "none", data.castObceList().size());
            return data;
        } finally {
            reader.close();
        }
    }

    private ParsedData doParse(XMLStreamReader reader) throws XMLStreamException {
        List<String> stack = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        ObecAccumulator obecAcc = new ObecAccumulator();
        ObecData obec = null;

        CastObceAccumulator castObceAcc = null;
        List<CastObceData> castObceList = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String localName = reader.getLocalName();
                    if ("CastObce".equals(localName)) {
                        castObceAcc = new CastObceAccumulator();
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
                    boolean insideCastObce = stack.contains("CastObce");

                    if ("Kod".equals(localName) && "Obec".equals(parent)) {
                        Long kod = parseLongOrNull(value);
                        if (insideCastObce) {
                            castObceAcc.kodObce = kod;
                        } else {
                            obecAcc.kod = kod;
                        }
                    } else if ("Nazev".equals(localName) && "Obec".equals(parent) && !insideCastObce) {
                        obecAcc.nazev = value;
                    } else if ("Kod".equals(localName) && "CastObce".equals(parent)) {
                        castObceAcc.kod = parseLongOrNull(value);
                    } else if ("Nazev".equals(localName) && "CastObce".equals(parent)) {
                        castObceAcc.nazev = value;
                    } else if ("Obec".equals(localName) && !insideCastObce) {
                        if (obec == null) {
                            obec = obecAcc.toCompleteDataOrNull();
                        }
                    } else if ("CastObce".equals(localName)) {
                        CastObceData candidate = castObceAcc.toCompleteDataOrNull();
                        if (candidate != null) {
                            castObceList.add(candidate);
                        }
                        castObceAcc = null;
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

    /** Mutable accumulator for the obec currently being read - one field to reset per new field, not several. */
    private static final class ObecAccumulator {
        private Long kod;
        private String nazev;

        private ObecData toCompleteDataOrNull() {
            ObecData candidate = new ObecData(kod, nazev);
            return candidate.isComplete() ? candidate : null;
        }
    }

    /** Mutable accumulator for the vf:CastObce element currently being read. */
    private static final class CastObceAccumulator {
        private Long kod;
        private String nazev;
        private Long kodObce;

        private CastObceData toCompleteDataOrNull() {
            CastObceData candidate = new CastObceData(kod, nazev, kodObce);
            return candidate.isComplete() ? candidate : null;
        }
    }
}
