package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.repository.TerminRepository;
import com.softwareprojekt.teambuilder.repository.VeranstaltungRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//Author: Fiona Sander
@Service
public class VeranstaltungService {

    @Autowired
    private VeranstaltungRepository veranstaltungRepository;
    @Autowired
    private BenutzerService benutzerService;
    private final TerminRepository terminRepository;
    @Autowired
    private TerminService terminService;
    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    public VeranstaltungService(VeranstaltungRepository veranstaltungRepository, TerminRepository terminRepository) {
        this.veranstaltungRepository = veranstaltungRepository;
        this.terminRepository = terminRepository;
    }

    public Veranstaltung findVeranstaltungById(long id) {
        return veranstaltungRepository.findById(id).orElse(null);
    }

    public Veranstaltung findVeranstaltungByTitel(String titel) {
        return veranstaltungRepository.findByTitel(titel).orElse(null);
    }


    public Veranstaltung findOrCreateVeranstaltung(String titel, String ects, long benutzerId) {
        Optional<Veranstaltung> existing = veranstaltungRepository.findByTitel(titel);
        if (existing.isPresent()) {
            return existing.get();
        }
        Optional<Benutzer> benutzer = benutzerService.findBenutzerByid(benutzerId);
        if (benutzer.isPresent()) {
            Benutzer b = benutzer.get();
            Veranstaltung neueVeranstaltung = new Veranstaltung(titel, ects, b);
            return veranstaltungRepository.save(neueVeranstaltung);
        }


        Veranstaltung neueVeranstaltung = new Veranstaltung(titel, ects);
        return veranstaltungRepository.save(neueVeranstaltung);
    }

    public void addTeilnehmerToVeranstaltung(Veranstaltung veranstaltung, Teilnehmer teilnehmer) {
        if (!veranstaltung.getTeilnehmer().contains(teilnehmer)) {
            veranstaltung.addTeilnehmer(teilnehmer);
            veranstaltungRepository.save(veranstaltung);
        }
    }

    public void addTeilnehmerToVeranstaltung(Veranstaltung veranstaltung, List<Teilnehmer> teilnehmerListe) {
        for (Teilnehmer t : teilnehmerListe) {
            if ((veranstaltung.getTeilnehmer() != null)) {
                if (!veranstaltung.getTeilnehmer().contains(t)){
                    veranstaltung.addTeilnehmer(t);
                    t.addVeranstaltung(veranstaltung);
                }

            }
        }
        veranstaltungRepository.save(veranstaltung);
    }

    public String getNaechsterTermin(Long veranstaltungId) {
        // Veranstaltung anhand der ID finden
        Optional<Veranstaltung> veranstaltungOpt = veranstaltungRepository.findById(veranstaltungId);

        if (veranstaltungOpt.isPresent()) {
            Veranstaltung veranstaltung = veranstaltungOpt.get();

            // Hole alle Termine dieser Veranstaltung
            List<Termin> termine = terminRepository.findByVeranstaltung(veranstaltung);

            if (termine == null || termine.isEmpty()) {
                return "Kein Termin";
            }

            return termine.stream()
                    .filter(termin -> {
                        LocalDate heute = LocalDate.now();
                        LocalTime jetzt = LocalTime.now();
                        LocalDate terminDatum = termin.getDatum();
                        LocalTime terminZeit = termin.getStartzeit();

                        return terminDatum.isAfter(heute)
                                || (terminDatum.isEqual(heute) && terminZeit.isAfter(jetzt));
                    })
                    .min(Comparator.comparing(Termin::getDatum)
                            .thenComparing(Termin::getStartzeit))
                    .map(termin -> FormatService.formatDate(termin.getDatum()))
                    .orElse("Kein zukünftiger Termin");
        }

        return "Veranstaltung nicht gefunden";
    }

    public void save(Veranstaltung veranstaltung) {
        veranstaltungRepository.save(veranstaltung);
    }

    public void delete(Veranstaltung veranstaltung) {
        veranstaltungRepository.delete(veranstaltung);
    }

    public List<Veranstaltung> findAllVeranstaltungen(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return veranstaltungRepository.findAll();
        } else {
            return veranstaltungRepository.search(filterText);
        }
    }

    @Transactional
    public void deleteVeranstaltungMitAbhaengigkeiten(Veranstaltung veranstaltung) {
        Veranstaltung managed = veranstaltungRepository.findById(veranstaltung.getId())
                .orElseThrow(() -> new RuntimeException("Veranstaltung nicht gefunden"));


        for (Termin termin : new ArrayList<>(managed.getTermine())) {
            terminService.deleteTerminMitAbhaengigkeiten(termin);
        }


        managed.clearTeilnehmer();
        veranstaltungRepository.save(managed);
        entityManager.flush();
        veranstaltungRepository.delete(managed);
    }
}


