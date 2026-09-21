package com.example.xmldbimporter;

import com.example.xmldbimporter.config.ImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point. This is a one-shot batch job (see {@link com.example.xmldbimporter.runner.ImportRunner}),
 * not a web service - the application exits once the import pipeline finishes.
 */
@SpringBootApplication
@EnableConfigurationProperties(ImportProperties.class)
public class XmlDbImporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlDbImporterApplication.class, args);
    }
}
