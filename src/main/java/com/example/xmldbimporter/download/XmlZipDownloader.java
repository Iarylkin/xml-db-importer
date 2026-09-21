package com.example.xmldbimporter.download;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public InputStream downloadXml(String zipUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(zipUrl)).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("Failed to download file, HTTP status " + response.statusCode() + " (" + zipUrl + ")");
        }

        ZipInputStream zip = new ZipInputStream(response.body());
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
                return zip;
            }
        }

        zip.close();
        throw new IOException("No XML file found inside the zip archive: " + zipUrl);
    }
}
