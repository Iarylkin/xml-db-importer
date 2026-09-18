package com.example.xmldbimporter;

import com.example.xmldbimporter.config.ImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ImportProperties.class)
public class XmlDbImporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlDbImporterApplication.class, args);
    }
}
