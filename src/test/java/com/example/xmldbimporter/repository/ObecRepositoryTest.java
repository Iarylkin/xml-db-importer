package com.example.xmldbimporter.repository;

import com.example.xmldbimporter.model.Obec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "classpath:db/init/schema.sql")
class ObecRepositoryTest {

    @Autowired
    private ObecRepository obecRepository;

    @Test
    void createsAndReadsObec() {
        obecRepository.save(new Obec(573060L, "Kopidlno"));

        Optional<Obec> found = obecRepository.findById(573060L);

        assertThat(found).isPresent();
        assertThat(found.get().getNazev()).isEqualTo("Kopidlno");
    }

    @Test
    void updatesObecByResavingSameKod() {
        obecRepository.save(new Obec(573060L, "Kopidlno"));

        obecRepository.save(new Obec(573060L, "Kopidlno (updated)"));

        assertThat(obecRepository.count()).isEqualTo(1);
        assertThat(obecRepository.findById(573060L).get().getNazev())
                .isEqualTo("Kopidlno (updated)");
    }

    @Test
    void deletesObec() {
        obecRepository.save(new Obec(573060L, "Kopidlno"));

        obecRepository.deleteById(573060L);

        assertThat(obecRepository.findById(573060L)).isEmpty();
    }
}
