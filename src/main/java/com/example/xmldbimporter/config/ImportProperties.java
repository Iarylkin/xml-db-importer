package com.example.xmldbimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param sourceUrl URL zazipovaného XML souboru ve výměnném formátu RÚIAN.
 */
@ConfigurationProperties(prefix = "import")
public record ImportProperties(String sourceUrl) {
}
