package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.repository.TeilnahmeRepository;
import com.softwareprojekt.teambuilder.repository.TeilnehmerRepository;
import com.softwareprojekt.teambuilder.repository.TerminRepository;
import com.softwareprojekt.teambuilder.repository.VeranstaltungRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
//Author: Thenujan Karunakumar
@Service
public class TeilnehmerService {

    @Autowired
    private TeilnehmerRepository teilnehmerRepository;

    @Autowired
    private VeranstaltungRepository veranstaltungRepository;

    @Autowired
    private TerminRepository terminRepository;
    @Autowired
    private TeilnahmeRepository teilnahmeRepository;
    @Autowired
    private TeilnahmeService teilnahmeService;
    @Autowired
    private GruppeService gruppeService;

    @PersistenceContext
    private EntityManager entityManager;

    public void save(Teilnehmer teilnehmer){

        if (teilnehmer == null) {
            System.out.println("Teilnehmer is null");
            return;
        }

        teilnehmerRepository.save(teilnehmer);
    }

    public Teilnehmer saveTeilnehmer(Teilnehmer teilnehmer) {
        if (teilnehmer == null) {
            System.out.println("Teilnehmer is null");
            return null;
        }

        return teilnehmerRepository.save(teilnehmer);
    }


    @Transactional
    public void deleteTeilnehmerMitAbhaengigkeiten(Teilnehmer teilnehmer) {

        Teilnehmer managed = teilnehmerRepository.findById(teilnehmer.getMatrnr())
                .orElseThrow(() -> new RuntimeException("Teilnehmer nicht gefunden"));


        teilnahmeService.deleteAllByTeilnehmer(managed);


        for (Termin termin : new ArrayList<>(managed.getTermine())) {
            termin.removeTeilnehmer(managed);
            terminRepository.save(termin);
        }


        for (Veranstaltung veranstaltung : new ArrayList<>(managed.getVeranstaltungen())) {
            veranstaltung.removeTeilnehmer(managed);
            veranstaltungRepository.save(veranstaltung);
        }


        teilnehmerRepository.delete(managed);
    }


    public Teilnehmer findOrCreateTeilnehmer(Long matrnr, String vorname, String nachname) {
        Teilnehmer existing = teilnehmerRepository.findTeilnehmerByMatrnr(matrnr);
        if (existing !=null) {
            return existing;
        }

        Teilnehmer neuerTeilnehmer = new Teilnehmer(matrnr);
        neuerTeilnehmer.setVorname(vorname);
        neuerTeilnehmer.setNachname(nachname);
        Teilnehmer r = teilnehmerRepository.save(neuerTeilnehmer);
        return r;
    }
    public Teilnehmer findOrCreateManaged(Teilnehmer teilnehmer) {
        return teilnehmerRepository.findById(teilnehmer.getMatrnr())
                .map(existing -> {
                    existing.setVorname(teilnehmer.getVorname());
                    existing.setNachname(teilnehmer.getNachname());
                    return teilnehmerRepository.save(existing); // Update
                })
                .orElseGet(() -> teilnehmerRepository.save(teilnehmer)); // Neu speichern
    }

    public List<Teilnehmer> findAllBenutzer(String filterText) {
        if(filterText == null || filterText.isEmpty()) {
            return teilnehmerRepository.findAll();
        }
        else {
            return teilnehmerRepository.search(filterText);
        }
    }

    public Teilnehmer findTeilnehmer(Long matrnr) {
        return this.teilnehmerRepository.findTeilnehmerByMatrnr(matrnr);
    }

    public List<Teilnehmer> findAllTeilnehmer(PageRequest pageRequest) {
        return teilnehmerRepository.findAll(pageRequest).getContent();
    }

    public List<Teilnehmer> findAllTeilnehmerByTermine(List<Termin> termine) {
        return teilnehmerRepository.findByTermineIn(termine);
    }
}
