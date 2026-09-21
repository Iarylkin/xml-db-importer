package com.example.xmldbimporter.repository;

import com.example.xmldbimporter.model.CastObce;
import com.example.xmldbimporter.model.Obec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "classpath:db/init/schema.sql")
class CastObceRepositoryTest {

    @Autowired
    private ObecRepository obecRepository;

    @Autowired
    private CastObceRepository castObceRepository;

    @Test
    void createsAndReadsCastObceWithObecRelation() {
        Obec obec = obecRepository.save(new Obec(573060L, "Kopidlno"));

        castObceRepository.save(new CastObce(111111L, "Kopidlno", obec));

        Optional<CastObce> found = castObceRepository.findById(111111L);
        assertThat(found).isPresent();
        assertThat(found.get().getNazev()).isEqualTo("Kopidlno");
        assertThat(found.get().getObec().getKod()).isEqualTo(573060L);
    }

    @Test
    void updatesCastObceByResavingSameKod() {
        Obec obec = obecRepository.save(new Obec(573060L, "Kopidlno"));
        castObceRepository.save(new CastObce(111111L, "Kopidlno", obec));

        castObceRepository.save(new CastObce(111111L, "Kopidlno (updated)", obec));

        assertThat(castObceRepository.count()).isEqualTo(1);
        assertThat(castObceRepository.findById(111111L).get().getNazev())
                .isEqualTo("Kopidlno (updated)");
    }

    @Test
    void deletesCastObce() {
        Obec obec = obecRepository.save(new Obec(573060L, "Kopidlno"));
        castObceRepository.save(new CastObce(111111L, "Kopidlno", obec));

        castObceRepository.deleteById(111111L);

        assertThat(castObceRepository.findById(111111L)).isEmpty();
    }
}
