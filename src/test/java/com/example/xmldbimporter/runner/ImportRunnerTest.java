package com.example.xmldbimporter.runner;

import com.example.xmldbimporter.config.ImportProperties;
import com.example.xmldbimporter.download.XmlZipDownloader;
import com.example.xmldbimporter.parser.ObecData;
import com.example.xmldbimporter.parser.ParsedData;
import com.example.xmldbimporter.parser.XmlParser;
import com.example.xmldbimporter.service.ImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportRunnerTest {

    @Mock
    private XmlZipDownloader downloader;

    @Mock
    private XmlParser parser;

    @Mock
    private ImportService importService;

    @Test
    void downloadsParsesAndSaves() throws Exception {
        ImportProperties properties = new ImportProperties("https://example.org/data.zip");
        InputStream xmlStream = new ByteArrayInputStream(new byte[0]);
        ParsedData parsedData = new ParsedData(new ObecData(573060L, "Kopidlno"), List.of());

        when(downloader.downloadXml("https://example.org/data.zip")).thenReturn(xmlStream);
        when(parser.parse(xmlStream)).thenReturn(parsedData);

        new ImportRunner(properties, downloader, parser, importService).run();

        verify(importService).save(parsedData);
    }

    @Test
    void usesUrlFromCommandLineArgumentWhenProvided() throws Exception {
        ImportProperties properties = new ImportProperties("https://example.org/default.zip");
        InputStream xmlStream = new ByteArrayInputStream(new byte[0]);
        ParsedData parsedData = new ParsedData(new ObecData(573060L, "Kopidlno"), List.of());

        when(downloader.downloadXml("https://example.org/override.zip")).thenReturn(xmlStream);
        when(parser.parse(xmlStream)).thenReturn(parsedData);

        new ImportRunner(properties, downloader, parser, importService).run("https://example.org/override.zip");

        verify(importService).save(parsedData);
    }

    @Test
    void propagatesDownloadFailure() throws Exception {
        ImportProperties properties = new ImportProperties("https://example.org/data.zip");
        when(downloader.downloadXml(any())).thenThrow(new IOException("boom"));

        ImportRunner runner = new ImportRunner(properties, downloader, parser, importService);

        assertThatThrownBy(runner::run).isInstanceOf(IOException.class);
    }
}
