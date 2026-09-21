package com.example.xmldbimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param sourceUrl URL of the zipped source XML file to import.
 */
@ConfigurationProperties(prefix = "import")
public record ImportProperties(String sourceUrl) {
}
