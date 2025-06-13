package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.repository.GruppeRepository;
import com.softwareprojekt.teambuilder.repository.TeilnahmeRepository;
import com.softwareprojekt.teambuilder.repository.TeilnehmerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

//Author: Thenujan Karunakumar
@Service
public class TeilnahmeService {

    @Autowired
    private TeilnahmeRepository teilnahmeRepository;

    @Autowired
    private GruppeRepository gruppeRepository;
    @Autowired
    private TeilnehmerRepository teilnehmerRepository;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TeilnehmerService teilnehmerService;
    @Autowired
    private GruppeService gruppeService;


    @Transactional
    public Teilnahme createTeilnahme(Gruppe gruppe, Teilnehmer teilnehmer, double leistungspunkte, double praesentationspunkte) {
        Gruppe managedGruppe = gruppeRepository.findById(gruppe.getId())
                .orElseThrow(() -> new RuntimeException("Gruppe nicht gefunden"));
        Teilnehmer managedTeilnehmer = teilnehmerRepository.findById(teilnehmer.getMatrnr())
                .orElseThrow(() -> new RuntimeException("Teilnehmer nicht gefunden"));

        System.out.println("Gruppe is managed: " + entityManager.contains(gruppe));
        System.out.println("ManagedGruppe is managed: " + entityManager.contains(managedGruppe));
        System.out.println("Teilnehmer is managed: " + entityManager.contains(teilnehmer));
        System.out.println("ManagedTeilnehmer is managed: " + entityManager.contains(managedTeilnehmer));

        Optional<Teilnahme> existing = teilnahmeRepository.findByGruppeAndTeilnehmer(managedGruppe, managedTeilnehmer);
        if (existing.isPresent()) {
            return existing.get(); // Teilnahme existiert schon, wir nutzen sie einfach
        }
        else {
            Teilnahme t = new Teilnahme();
            t.setGruppe(managedGruppe);
            t.setTeilnehmer(managedTeilnehmer);
            t.setLeistungspunkte(leistungspunkte);
            t.setPraesentationspunkte(praesentationspunkte);
            return teilnahmeRepository.save(t);
        }

    }

    public List<Teilnahme> findAllByGruppe(Gruppe gruppe) {
        return teilnahmeRepository.findAllByGruppe(gruppe);
    }

    public void save(Teilnahme teilnahme) {
        teilnahmeRepository.save(teilnahme);
    }

    public void delete(Teilnahme teilnahme) {
        teilnahmeRepository.delete(teilnahme);
    }

    @Transactional
    public void deleteAllByTeilnehmer(Teilnehmer teilnehmer) {
        Teilnehmer managedTeilnehmer= teilnehmerService.findTeilnehmer(teilnehmer.getMatrnr());
        List<Teilnahme> teilnahmen = teilnahmeRepository.findAllByTeilnehmer(teilnehmer);
        //für jede Teilnahme die Referenz beim teilnehmer entfernen
        teilnahmen.forEach(t -> {
            managedTeilnehmer.removeTeilnahme(t);
            if(t.getGruppe() != null) {
                Gruppe gruppe = t.getGruppe();
                gruppe.removeTeilnahme(t);
                gruppeService.save(gruppe); // Gruppe aktualisieren
            }
        });
        teilnahmeRepository.deleteAll(teilnahmen);
        entityManager.flush();
        List<Teilnahme> neuteilnahmen = teilnahmeRepository.findAllByTeilnehmer(teilnehmer);
        System.out.println("Neuteilnahmen: " + neuteilnahmen);
    }
    @Transactional
    public void deleteAllByGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        List<Teilnahme> zuLoeschen = teilnahmeRepository.findAllByGruppe_Gruppenarbeit(gruppenarbeit);

        for(Teilnahme teilnahme : zuLoeschen) {
            Teilnehmer teilnehmer = teilnahme.getTeilnehmer();
            if (teilnehmer != null) {
                teilnehmer.removeTeilnahme(teilnahme);
                teilnehmerService.save(teilnehmer); // Teilnehmer aktualisieren
            }
            Gruppe gruppe = teilnahme.getGruppe();
            if (gruppe != null) {
                gruppe.removeTeilnahme(teilnahme);
                gruppeService.save(gruppe); // Gruppe aktualisieren
            }
        }


        entityManager.flush(); //


        zuLoeschen = teilnahmeRepository.findAllByGruppe_Gruppenarbeit(gruppenarbeit);
        teilnahmeRepository.deleteAll(zuLoeschen);
        entityManager.flush();
    }


    public Teilnahme findById(long id) {
        return teilnahmeRepository.findById(id);
    }

    public List<Teilnahme> findeAlleByGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        return this.teilnahmeRepository.findAllByGruppe_Gruppenarbeit(gruppenarbeit);
    }

    public void saveAll(List<Teilnahme> teilnahmen) {
        teilnahmeRepository.saveAll(teilnahmen);
    }
}

