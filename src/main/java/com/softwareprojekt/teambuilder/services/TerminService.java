package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.repository.TerminRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//Author: Thenujan Karunakumar
@Service
public class TerminService {

    @Autowired
    private TerminRepository terminRepository;
    @Autowired
    private GruppenarbeitService gruppenarbeitService;


    public void save(Termin termin) {

        if (termin == null) {
            System.out.println("Termin is null");
            return;
        }
        terminRepository.save(termin);
    }

    public Termin findOrCreateTermin(LocalDate datum, LocalTime startzeit, LocalTime endzeit, Veranstaltung veranstaltung) {
        Optional<Termin> existing = terminRepository.findByDatumAndStartzeitAndVeranstaltung(datum, startzeit, veranstaltung);
        if (existing.isPresent()) {
            return existing.get();
        }

        Termin neuerTermin = new Termin(datum, startzeit, endzeit);
        neuerTermin.setVeranstaltung(veranstaltung);
        return terminRepository.save(neuerTermin);
    }

    public void addTeilnehmerToTermin(Termin termin, List<Teilnehmer> teilnehmerList) {
        boolean modified = false;

        for (Teilnehmer teilnehmer : teilnehmerList) {
            if (!termin.getTeilnehmer().contains(teilnehmer)) {
                termin.addTeilnehmer(teilnehmer);
                modified = true;
            }
        }

        if (modified || termin.getId() == 0) {
            terminRepository.save(termin);
        }
    }

    public void addTeilnehmerToTermin(Termin termin, Teilnehmer teilnehmer) {
        boolean modified = false;
        if (!termin.getTeilnehmer().contains(teilnehmer)) {
            termin.addTeilnehmer(teilnehmer);
            modified = true;
        }


        if (modified || termin.getId() == 0) {
            terminRepository.save(termin);
        }
    }

    public List<Termin> findAllTermineByVeranstaltung(Veranstaltung veranstaltung) {
        return this.terminRepository.findByVeranstaltung(veranstaltung);
    }

    public void delete(Termin termin) {
        terminRepository.delete(termin);
    }



    public Termin findTerminById(long id) {
        return terminRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteTerminMitAbhaengigkeiten(Termin termin) {
        Termin managed = terminRepository.findById(termin.getId())
                .orElseThrow(() -> new RuntimeException("Termin nicht gefunden"));

        for (Gruppenarbeit g : new ArrayList<>(managed.getGruppenarbeiten())) {
            gruppenarbeitService.deleteGruppenarbeitMitAbhaengigkeiten(g);
        }

        Veranstaltung veranstaltung = managed.getVeranstaltung();
        if (veranstaltung != null) {
            veranstaltung.removeTermin(managed); // Liste sauber halten
            managed.setVeranstaltung(null);
        }

        managed.clearTeilnehmer();

        terminRepository.delete(managed);
    }


    public List<Termin> findeAlleTermineByVeranstaltung(Veranstaltung veranstaltung) {
        if (veranstaltung == null) {
            return new ArrayList<>();
        }
        return terminRepository.findByVeranstaltung(veranstaltung);
    }
    public boolean terminExistiert(Termin termin) {
        return terminRepository.existsByDatumAndStartzeitAndEndzeit(
                termin.getDatum(),
                termin.getStartzeit(),
                termin.getEndzeit()
        );
    }

}
