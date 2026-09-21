package com.example.xmldbimporter.runner;

import com.example.xmldbimporter.config.ImportProperties;
import com.example.xmldbimporter.download.XmlZipDownloader;
import com.example.xmldbimporter.parser.ParsedData;
import com.example.xmldbimporter.parser.XmlParser;
import com.example.xmldbimporter.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Wires the whole pipeline together at application startup: download the
 * zipped XML, parse it, and save the result to the DB. This is a one-shot
 * batch job, not a web service - the application exits once run() returns.
 */
@Component
public class ImportRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ImportRunner.class);

    private final ImportProperties properties;
    private final XmlZipDownloader downloader;
    private final XmlParser parser;
    private final ImportService importService;

    public ImportRunner(ImportProperties properties, XmlZipDownloader downloader,
                         XmlParser parser, ImportService importService) {
        this.properties = properties;
        this.downloader = downloader;
        this.parser = parser;
        this.importService = importService;
    }

    @Override
    public void run(String... args) throws Exception {
        String url = args.length > 0 ? args[0] : properties.sourceUrl();
        LOG.info("Downloading XML from {}", url);

        try (InputStream xml = downloader.downloadXml(url)) {
            ParsedData data = parser.parse(xml);
            importService.save(data);
        } catch (Exception e) {
            LOG.error("Import failed: {}", e.getMessage());
            throw e;
        }
    }
}
