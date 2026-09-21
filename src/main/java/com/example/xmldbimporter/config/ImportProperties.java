package com.example.xmldbimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param sourceUrl URL of the zipped XML file in ČÚZK's RÚIAN exchange format (VFR).
 */
@ConfigurationProperties(prefix = "import")
public record ImportProperties(String sourceUrl) {
}
