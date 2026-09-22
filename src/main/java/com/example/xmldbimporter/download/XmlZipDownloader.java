package com.example.xmldbimporter.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads a zipped file from the given URL and streams out the first XML
 * entry found inside it. Downloading and unzipping happen on the fly, one
 * chained InputStream (HTTP response -&gt; ZIP -&gt; XML), without buffering
 * the whole archive on disk or in memory.
 */
@Component
public class XmlZipDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(XmlZipDownloader.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * @param zipUrl URL of the zip archive to download
     * @return an open stream positioned at the first {@code .xml} entry found in the archive;
     *     the caller is responsible for closing it
     * @throws IOException if the HTTP response status isn't 200, or the archive contains no XML entry
     */
    public InputStream downloadXml(String zipUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(zipUrl)).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("Failed to download file, HTTP status " + response.statusCode() + " (" + zipUrl + ")");
        }

        ZipInputStream zip = new ZipInputStream(response.body());
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
                    LOG.debug("Found XML entry '{}' in the downloaded archive", entry.getName());
                    return zip;
                }
            }
        } catch (IOException e) {
            zip.close();
            throw e;
        }

        zip.close();
        throw new IOException("No XML file found inside the zip archive: " + zipUrl);
    }
}
