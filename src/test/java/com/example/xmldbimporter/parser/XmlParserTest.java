package com.example.xmldbimporter.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class XmlParserTest {

    private final XmlParser parser = new XmlParser();

    @Test
    void parsesObecAndCastObceWhileIgnoringUnrelatedNestedReferences() throws Exception {
        try (InputStream xml = getClass().getResourceAsStream("/sample-obec-export.xml")) {
            ParsedData data = parser.parse(xml);

            assertThat(data.obec()).isNotNull();
            assertThat(data.obec().kod()).isEqualTo(573060L);
            assertThat(data.obec().nazev()).isEqualTo("Kopidlno");

            // Exactly the 3 real vf:CastObce elements - the incomplete reference
            // nested inside vf:StavebniObjekt (kod only, no nazev) must be discarded.
            assertThat(data.castObceList()).hasSize(3);
            assertThat(data.castObceList())
                    .extracting(CastObceData::nazev)
                    .containsExactlyInAnyOrder("Kopidlno", "Ledkov", "Drahoraz");
            assertThat(data.castObceList())
                    .allMatch(c -> c.kodObce().equals(573060L));
        }
    }
}
