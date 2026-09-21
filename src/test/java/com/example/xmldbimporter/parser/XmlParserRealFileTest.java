package com.example.xmldbimporter.parser;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parses the real export for Kopidlno (as distributed at
 * https://www.smartform.cz/download/kopidlno.xml.zip), unlike
 * {@link XmlParserTest} which uses a trimmed synthetic fixture. Guards
 * against the parser's assumptions (element names, namespaces, decoy
 * references) drifting from what the real file actually contains.
 */
class XmlParserRealFileTest {

    private final XmlParser parser = new XmlParser();

    @Test
    void parsesTheRealKopidlnoExport() throws Exception {
        try (InputStream zipStream = getClass().getResourceAsStream("/kopidlno.xml.zip");
             ZipInputStream zip = new ZipInputStream(zipStream)) {

            ZipEntry entry = zip.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.getName()).endsWith(".xml");

            ParsedData data = parser.parse(zip);

            assertThat(data.obec()).isNotNull();
            assertThat(data.obec().kod()).isEqualTo(573060L);
            assertThat(data.obec().nazev()).isEqualTo("Kopidlno");

            assertThat(data.castObceList()).hasSize(5);
            assertThat(data.castObceList())
                    .extracting(CastObceData::nazev)
                    .containsExactlyInAnyOrder("Kopidlno", "Ledkov", "Mlýnec", "Drahoraz", "Pševes");
            assertThat(data.castObceList())
                    .allMatch(c -> c.kodObce().equals(573060L));
        }
    }
}
