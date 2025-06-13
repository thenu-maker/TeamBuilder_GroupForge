package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.repository.GruppeRepository;
import com.softwareprojekt.teambuilder.repository.GruppenarbeitRepository;
import com.softwareprojekt.teambuilder.repository.TeilnahmeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
//Author: Thenujan Karunakumar
@Service
public class GruppenarbeitService {

    @Autowired
    private GruppenarbeitRepository gruppenarbeitRepository;

    @Autowired
    private GruppeRepository gruppeRepository;

    @Autowired
    private TeilnahmeRepository teilnahmeRepository;

    @Autowired
    private GruppeService gruppeService;

    @Autowired
    private TeilnahmeService teilnahmeService;

    @PersistenceContext
    private EntityManager entityManager;

    public Gruppenarbeit createGruppenarbeit(String titel, Termin termin) {
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit(titel, termin);
        return gruppenarbeitRepository.save(gruppenarbeit);
}


    public Gruppenarbeit findGruppenarbeitById(long id) {
        return gruppenarbeitRepository.findById(id).orElseThrow(() -> new RuntimeException("Gruppenarbeit nicht gefunden"));
    }

    public Gruppenarbeit findOrCreateGruppenarbeit(String titel, Termin termin) {
        Gruppenarbeit vorhandene = gruppenarbeitRepository.findByTitelAndTermin(titel, termin);
        if (vorhandene != null) return vorhandene;

        Gruppenarbeit neue = new Gruppenarbeit(titel, termin);
        return gruppenarbeitRepository.save(neue);
    }

    public void erstelleGruppenNachAnzahl(Gruppenarbeit gruppenarbeit, int anzahlGruppen) {
        if (anzahlGruppen <= 0) {
            throw new IllegalArgumentException("Die Anzahl der Gruppen muss größer als 0 sein");
        }


        Gruppenarbeit managedGruppenarbeit = gruppenarbeitRepository.findById(gruppenarbeit.getId())
                .orElseThrow(() -> new IllegalArgumentException("Gruppenarbeit nicht gefunden"));

        // Teilnehmer direkt aus dem Termin der gemanagten Gruppenarbeit holen
        List<Teilnehmer> teilnehmer = new ArrayList<>(managedGruppenarbeit.getTermin().getTeilnehmer());

        if (teilnehmer.isEmpty()) {
            throw new IllegalStateException("Keine Teilnehmer vorhanden");
        }

        if (anzahlGruppen > teilnehmer.size()) {
            throw new IllegalArgumentException("Es können nicht mehr Gruppen als Teilnehmer erstellt werden");
        }


        Collections.shuffle(teilnehmer);

        List<Gruppe> erstellteGruppen = new ArrayList<>();

        for (int i = 0; i < anzahlGruppen; i++) {
            Gruppe gruppe = new Gruppe();
            gruppe.setGruppenarbeit(managedGruppenarbeit);
            gruppe.setGruppenname("Gruppe " + (i + 1));
            gruppe.setTeilnahmen(new ArrayList<>());
            erstellteGruppen.add(gruppe);
        }

        //Gruppen speichern, bevor sie in Teilnahmen verwendet werden
        gruppeRepository.saveAll(erstellteGruppen);

        int currentGruppe = 0;
        List<Teilnahme> neueTeilnahmen = new ArrayList<>();


        for (Teilnehmer t : teilnehmer) {
            Gruppe gruppe = erstellteGruppen.get(currentGruppe);

            Teilnahme neueTeilnahme = new Teilnahme();
            neueTeilnahme.setTeilnehmer(t);
            neueTeilnahme.setGruppe(gruppe);

            gruppe.addTeilnahme(neueTeilnahme);
            neueTeilnahmen.add(neueTeilnahme);

            currentGruppe = (currentGruppe + 1) % anzahlGruppen;

        }

        teilnahmeRepository.saveAll(neueTeilnahmen);
    }

    @Transactional
    public void loescheVorhandeneGruppen(Gruppenarbeit gruppenarbeit) {

        List<Gruppe> alteGruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        List<Teilnahme> alteTeilnahme= teilnahmeRepository.findByGruppeIn(alteGruppen);
        for (Teilnahme t : alteTeilnahme) {
            t.setGruppe(null);
            teilnahmeService.delete(t);
        }

        gruppeRepository.deleteAll(alteGruppen);
    }






    public List<Integer> berechneSinnvolleGruppenzahlen(int teilnehmerAnzahl) {
        List<Integer> gruppenzahlen = new ArrayList<>();
        for (int i = 1; i <= teilnehmerAnzahl; i++) {
            int basisGroesse = teilnehmerAnzahl / i;
            int rest = teilnehmerAnzahl % i;
            if (basisGroesse == 0) break;
            if (rest == 0 || rest <= i) {
                gruppenzahlen.add(i);
            }
        }
        return gruppenzahlen;
    }

    public void aendereGruppeVonTeilnahme(Long teilnahmeId, String neueGruppenname) {
        Teilnahme teilnahme = teilnahmeRepository.findById(teilnahmeId)
                .orElseThrow(() -> new RuntimeException("Teilnahme nicht gefunden"));

        Gruppe neueGruppe = gruppeRepository.findByGruppennameAndGruppenarbeit(
                neueGruppenname, teilnahme.getGruppe().getGruppenarbeit());

        if (neueGruppe == null) {
            throw new RuntimeException("Zielgruppe nicht gefunden.");
        }

        teilnahme.setGruppe(neueGruppe);
        teilnahmeRepository.save(teilnahme);
    }

    public void speichereTeilnahmenMitGruppen(List<Teilnahme> teilnahmen) {
        for (Teilnahme t : teilnahmen) {
            teilnahmeRepository.save(t);
        }
    }

    public Gruppenarbeit getVorherigeGruppenarbeit(Gruppenarbeit neueGruppenarbeit) {
        List<Gruppenarbeit> gruppenarbeitenImTermin = gruppenarbeitRepository.findByTermin(neueGruppenarbeit.getTermin());

        gruppenarbeitenImTermin.sort(Comparator.comparingLong(Gruppenarbeit::getId));

        for (int i = 1; i < gruppenarbeitenImTermin.size(); i++) {
            if (gruppenarbeitenImTermin.get(i).getId()==(neueGruppenarbeit.getId())) {
                return gruppenarbeitenImTermin.get(i - 1);
            }
        }

        return null;
    }

    public void uebernehmeAlteKonstellation(Gruppenarbeit aktuelleGruppenarbeit, Gruppenarbeit alteGruppenarbeit) {

        List<Gruppe> alteGruppen = gruppeRepository.findByGruppenarbeit(alteGruppenarbeit);
        for (Gruppe alteGruppe : alteGruppen) {
            Gruppe neueGruppe = new Gruppe();
            neueGruppe.setGruppenarbeit(aktuelleGruppenarbeit);
            neueGruppe.setGruppenname(alteGruppe.getGruppenname());
            neueGruppe.setTeilnahmen(new ArrayList<>());

            for (Teilnahme alteTeilnahme : alteGruppe.getTeilnahmen()) {
                Teilnahme neueTeilnahme = new Teilnahme();
                neueTeilnahme.setTeilnehmer(alteTeilnahme.getTeilnehmer());
                neueTeilnahme.setGruppe(neueGruppe);
                neueGruppe.addTeilnahme(neueTeilnahme);
            }

            gruppeRepository.save(neueGruppe);
        }
    }

    public List<Gruppenarbeit> findByTermin(Termin termin) {
        return gruppenarbeitRepository.findByTermin(termin);
    }

    public void save(Gruppenarbeit gruppenarbeit) {
        gruppenarbeitRepository.save(gruppenarbeit);
    }

    @Transactional
    public void deleteGruppenarbeitMitAbhaengigkeiten(Gruppenarbeit gruppenarbeit) {
        Gruppenarbeit managed = gruppenarbeitRepository.findById(gruppenarbeit.getId())
                .orElseThrow(() -> new RuntimeException("Gruppenarbeit nicht gefunden"));


        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(managed);
        teilnahmeService.deleteAllByGruppenarbeit(managed);
        gruppeService.deleteAllByGruppenarbeitohneTeilnahmen(managed);
        managed = findGruppenarbeitById(managed.getId());

        Termin termin = managed.getTermin();
        termin.removeGruppenarbeit(managed);
        entityManager.flush();


        gruppenarbeitRepository.delete(managed);
    }



    public List<Gruppenarbeit> findeAlleGruppenarbeitenByTermin(Termin termin) {
        return gruppenarbeitRepository.findByTermin(termin);
    }

    public List<Gruppe> erstelleLeereGruppenNachAnzahl(Gruppenarbeit aktuelleGruppenarbeit, int anzahl) {
        if (anzahl <= 0) {
            throw new IllegalArgumentException("Die Anzahl der Gruppen muss größer als 0 sein");
        }
        List<Gruppe> erstellteGruppen = new ArrayList<>();

        for (int i = 0; i < anzahl; i++) {
            Gruppe gruppe = new Gruppe();
            gruppe.setGruppenarbeit(aktuelleGruppenarbeit);
            gruppe.setGruppenname("Gruppe " + (i + 1));
            gruppe.setTeilnahmen(new ArrayList<>());
            erstellteGruppen.add(gruppe);
        }

        return gruppeRepository.saveAll(erstellteGruppen);
    }

    public List<Gruppenarbeit> findByVeranstaltungId(Long veranstaltungId) {
        return gruppenarbeitRepository.findByTermin_Veranstaltung_Id(veranstaltungId);
    }

    @Transactional
    public void uebernehmeGruppenVon(Gruppenarbeit quelleP, Gruppenarbeit zielP) {
        Gruppenarbeit quelle = findGruppenarbeitById(quelleP.getId());
        Gruppenarbeit ziel = findGruppenarbeitById(zielP.getId());
        teilnahmeService.deleteAllByGruppenarbeit(ziel);

        loescheVorhandeneGruppen(ziel);
        List<Teilnahme> quelleTeilnahmen = teilnahmeService.findeAlleByGruppenarbeit(quelle);

        Map<String, Gruppe> gruppenMap = new HashMap<>();

        for (Teilnahme t : quelleTeilnahmen) {
            String gruppenname = t.getGruppe().getGruppenname();


            Gruppe zielGruppe = gruppenMap.computeIfAbsent(gruppenname, name -> {
                Gruppe neueGruppe = new Gruppe();
                neueGruppe.setGruppenname(name);
                neueGruppe.setGruppenarbeit(ziel);
                return gruppeRepository.save(neueGruppe);
            });

            Teilnahme neueTeilnahme = new Teilnahme();
            neueTeilnahme.setGruppe(zielGruppe);
            neueTeilnahme.setTeilnehmer(t.getTeilnehmer());

            teilnahmeRepository.save(neueTeilnahme);
        }
    }
}


