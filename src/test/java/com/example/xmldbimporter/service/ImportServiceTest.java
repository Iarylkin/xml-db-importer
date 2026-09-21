package com.example.xmldbimporter.service;

import com.example.xmldbimporter.model.CastObce;
import com.example.xmldbimporter.parser.CastObceData;
import com.example.xmldbimporter.parser.ObecData;
import com.example.xmldbimporter.parser.ParsedData;
import com.example.xmldbimporter.repository.CastObceRepository;
import com.example.xmldbimporter.repository.ObecRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(scripts = "classpath:db/init/schema.sql")
class ImportServiceTest {

    @Autowired
    private ObecRepository obecRepository;

    @Autowired
    private CastObceRepository castObceRepository;

    private ImportService importService;

    @Test
    void savesObecAndItsCastObceList() {
        importService = new ImportService(obecRepository, castObceRepository);

        ParsedData data = new ParsedData(
                new ObecData(573060L, "Kopidlno"),
                List.of(
                        new CastObceData(111111L, "Kopidlno", 573060L),
                        new CastObceData(222222L, "Mokřice", 573060L)
                )
        );

        importService.save(data);

        assertThat(obecRepository.findById(573060L)).isPresent();
        List<CastObce> castObceList = castObceRepository.findAll();
        assertThat(castObceList).hasSize(2);
        assertThat(castObceList)
                .allSatisfy(c -> assertThat(c.getObec().getKod()).isEqualTo(573060L));
    }

    @Test
    void rejectsParsedDataWithoutObec() {
        importService = new ImportService(obecRepository, castObceRepository);

        ParsedData data = new ParsedData(null, List.of());

        assertThatThrownBy(() -> importService.save(data))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCastObceReferencingADifferentObecAndSavesNothing() {
        importService = new ImportService(obecRepository, castObceRepository);

        ParsedData data = new ParsedData(
                new ObecData(573060L, "Kopidlno"),
                List.of(new CastObceData(111111L, "Kopidlno", 999999L))
        );

        assertThatThrownBy(() -> importService.save(data))
                .isInstanceOf(IllegalStateException.class);

        assertThat(obecRepository.findAll()).isEmpty();
        assertThat(castObceRepository.findAll()).isEmpty();
    }
}
