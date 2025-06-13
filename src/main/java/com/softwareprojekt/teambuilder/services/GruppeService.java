package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.repository.GruppeRepository;
import com.softwareprojekt.teambuilder.repository.GruppenarbeitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
//Author: Thenujan Karunakumar
@Service
public class GruppeService {

    @Autowired
    private GruppeRepository gruppeRepository;
    @Autowired
    private TeilnahmeService teilnahmeService;



    public Gruppe findOrCreateGruppe(String gruppenname, Gruppenarbeit gruppenarbeit) {
        Gruppe vorhandene = gruppeRepository.findByGruppennameAndGruppenarbeit(gruppenname, gruppenarbeit);
        if (vorhandene != null) return vorhandene;

        Gruppe neue = new Gruppe();
        neue.setGruppenname(gruppenname);
        neue.setGruppenarbeit(gruppenarbeit);
        return gruppeRepository.save(neue);
    }

    public List<Gruppe> findAllByGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        return gruppeRepository.findAllByGruppenarbeit(gruppenarbeit);
    }

    public List<Gruppe> findAllByGruppenarbeitId(Long gruppenarbeitId) {
        return gruppeRepository.findAllByGruppenarbeitId(gruppenarbeitId);
    }

    public void save(Gruppe gruppe) {
        gruppeRepository.save(gruppe);
    }


    public void loescheLeereGruppen(Gruppenarbeit gruppenarbeit) {
        
        List<Gruppe> alleGruppenInGruppenarbeit = gruppeRepository.findAllByGruppenarbeit(gruppenarbeit);

        for (Gruppe gruppe : alleGruppenInGruppenarbeit) {
            List<Teilnahme> teilnahmen = teilnahmeService.findAllByGruppe(gruppe);
            if (teilnahmen == null || teilnahmen.isEmpty()) {
                gruppeRepository.delete(gruppe);
            }
        }
    }




    @Transactional
    public void deleteAllByGruppenarbeitohneTeilnahmen(Gruppenarbeit gruppenarbeit) {

        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        for (Gruppe gruppe : gruppen) {

            for (Teilnahme t : new ArrayList<>(gruppe.getTeilnahmen())) {
                gruppe.removeTeilnahme(t);
            }


            gruppeRepository.delete(gruppe);
        }
        gruppenarbeit.deleteAllGruppen();
    }

    @Transactional
    public void deleteAllByGruppenarbeitmitTeilnahmen(Gruppenarbeit gruppenarbeit) {

        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        for (Gruppe gruppe : gruppen) {

            for (Teilnahme t : new ArrayList<>(gruppe.getTeilnahmen())) {
                gruppe.removeTeilnahme(t); // → orphanRemoval oder manuelles delete muss hier greifen
                teilnahmeService.delete(t);

            }


            gruppeRepository.delete(gruppe);
        }


        gruppenarbeit.deleteAllGruppen();
    }


    public List<Gruppe> findeAlleByGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        return this.gruppeRepository.findByGruppenarbeit(gruppenarbeit);
    }

    public void aendereAnmerkungVonGruppe(Long gruppenId, String neueAnmerkung) {
        Gruppe gruppe = gruppeRepository.findById(gruppenId)
                .orElseThrow(() -> new IllegalArgumentException("Gruppe mit ID " + gruppenId + " nicht gefunden."));
        if (gruppe != null) {
            gruppe.setAnmerkung(neueAnmerkung);
            gruppeRepository.save(gruppe);
        } else {
            throw new IllegalArgumentException("Gruppe mit ID " + gruppenId + " nicht gefunden.");
        }
    }

    public void saveGruppe(Gruppe gruppe) {
        if (gruppe != null) {
            gruppeRepository.save(gruppe);
        } else {
            throw new IllegalArgumentException("Gruppe darf nicht null sein.");
        }
    }
}

