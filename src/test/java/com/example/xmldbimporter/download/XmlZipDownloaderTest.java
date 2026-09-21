package com.example.xmldbimporter.download;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlZipDownloaderTest {

    private HttpServer server;

    private final XmlZipDownloader downloader = new XmlZipDownloader();

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void downloadsAndUnzipsTheXmlEntry() throws Exception {
        server = startServer(200, zipOf("data.xml", "<root>hello</root>"));

        try (InputStream xml = downloader.downloadXml(baseUrl())) {
            assertThat(new String(xml.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("<root>hello</root>");
        }
    }

    @Test
    void failsOnNonOkHttpStatus() throws Exception {
        server = startServer(500, new byte[0]);

        assertThatThrownBy(() -> downloader.downloadXml(baseUrl())).isInstanceOf(IOException.class);
    }

    @Test
    void failsWhenZipHasNoXmlEntry() throws Exception {
        server = startServer(200, zipOf("data.txt", "not xml"));

        assertThatThrownBy(() -> downloader.downloadXml(baseUrl())).isInstanceOf(IOException.class);
    }

    private HttpServer startServer(int status, byte[] body) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.createContext("/file.zip", exchange -> {
            if (body.length == 0) {
                exchange.sendResponseHeaders(status, -1);
            } else {
                exchange.sendResponseHeaders(status, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.getResponseBody().close();
        });
        httpServer.start();
        return httpServer;
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/file.zip";
    }

    private byte[] zipOf(String entryName, String content) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }
}
