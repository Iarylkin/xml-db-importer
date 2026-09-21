package com.example.xmldbimporter.service;

import com.example.xmldbimporter.model.CastObce;
import com.example.xmldbimporter.model.Obec;
import com.example.xmldbimporter.parser.CastObceData;
import com.example.xmldbimporter.parser.ParsedData;
import com.example.xmldbimporter.repository.CastObceRepository;
import com.example.xmldbimporter.repository.ObecRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates a {@link ParsedData} result and persists it: the obec, then its cast obce records,
 * in one transaction.
 */
@Service
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    private final ObecRepository obecRepository;
    private final CastObceRepository castObceRepository;

    public ImportService(ObecRepository obecRepository, CastObceRepository castObceRepository) {
        this.obecRepository = obecRepository;
        this.castObceRepository = castObceRepository;
    }

    /**
     * Validates {@code data} and saves it. Validation runs to completion before any repository
     * call, so a rejected import never writes a partial result.
     *
     * @throws IllegalStateException if the obec is missing/incomplete, a cast obce references
     *     a different obec than the one that was parsed, or two cast obce share the same kod
     */
    @Transactional
    public void save(ParsedData data) {
        if (data.obec() == null) {
            throw new IllegalStateException("XML does not contain a complete vf:Obec element (kod and nazev)");
        }

        Long obecKod = data.obec().kod();
        Set<Long> seenCastObceKod = new HashSet<>();
        for (CastObceData castObceData : data.castObceList()) {
            if (!obecKod.equals(castObceData.kodObce())) {
                throw new IllegalStateException(
                        "CastObce with kod " + castObceData.kod() + " references obec " + castObceData.kodObce()
                                + ", but the parsed XML's obec has kod " + obecKod);
            }
            if (!seenCastObceKod.add(castObceData.kod())) {
                throw new IllegalStateException(
                        "Duplicate CastObce kod " + castObceData.kod() + " found in the parsed XML");
            }
        }

        if (data.castObceList().isEmpty()) {
            LOG.warn("XML for obec '{}' (kod {}) contains no cast obce records", data.obec().nazev(), obecKod);
        }

        Obec obec = obecRepository.save(new Obec(obecKod, data.obec().nazev()));

        List<CastObce> castObceList = data.castObceList().stream()
                .map(c -> new CastObce(c.kod(), c.nazev(), obec))
                .toList();
        castObceRepository.saveAll(castObceList);

        LOG.info("Saved to DB: obec '{}' (kod {}) with {} cast obce record(s)",
                obec.getNazev(), obec.getKod(), castObceList.size());
    }
}
