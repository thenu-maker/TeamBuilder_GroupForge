package com.softwareprojekt.teambuilder;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.repository.*;
import com.softwareprojekt.teambuilder.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//Author: Tolga Cenk Kilic, Thenujan Karunakumar
@SpringBootTest
class TeambuilderApplicationTests {

    @Autowired
    private VeranstaltungRepository veranstaltungRepository;

    @Autowired
    private TeilnehmerRepository teilnehmerRepository;

    @Autowired
    private TerminRepository terminRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private TeilnahmeRepository teilnahmeRepository;

    @Autowired
    private GruppenarbeitRepository gruppenarbeitRepository;

    @Autowired
    private TeilnehmerService teilnehmerService;

    @Autowired
    private VeranstaltungService veranstaltungService;

    @Autowired
    private TerminService terminService;

    @Autowired
    private GruppenarbeitService gruppenarbeitService;
    @Autowired
    private GruppeRepository gruppeRepository;
    @Autowired
    private GruppeService gruppeService;
    @Autowired
    private TeilnahmeService teilnahmeService;

    //Veranstaltung anlegen
    @Test
    void erolgreichVeranstaltungAnlegen() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testbenutzer", "Test", "Benutzer", "testbenutzer", Benutzer.Role.PROFESSOR));
        Veranstaltung veranstaltung = new Veranstaltung("Materialwirtschaft", "Wintersemester 2025", benutzer);
        veranstaltungRepository.save(veranstaltung);

        System.out.println("Veranstaltung erfolgreich angelegt: " + veranstaltung.getTitel());
        assertTrue(veranstaltungRepository.existsById(veranstaltung.getId()));
    }

    @Test
    void fehlerhaftVeranstaltungAnlegen() {
        Veranstaltung veranstaltung = new Veranstaltung("", "");

        try {
            veranstaltungRepository.save(veranstaltung);
            fail("Die Veranstaltung sollte aufgrund von Validierungsfehlern nicht gespeichert werden!"); // Fehlschlag, wenn es kein Exception gibt
        } catch (Exception e) {
            System.out.println(" Veranstaltung konnte nicht angelegt werden.");
            // Validieren, dass die Veranstaltung nicht in der Datenbank existiert
            assertFalse(veranstaltungRepository.findByTitel("").isPresent());
        }
    }

    @Test
    void erfolgreicheTeilnehmerAnlegen() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testbenutzer", "Test", "Benutzer", "testbenutzer", Benutzer.Role.PROFESSOR));
        Veranstaltung veranstaltung = veranstaltungRepository.save(new Veranstaltung("Materialwirtschaft", "Wintersemester 2025", benutzer));

        Teilnehmer teilnehmer = teilnehmerService.findOrCreateTeilnehmer(54322343L, "Test", "Ta");
        veranstaltung.addTeilnehmer(teilnehmer);
        veranstaltungRepository.save(veranstaltung);

        System.out.println("Teilnehmer erfolgreich angelegt: " + teilnehmer.getVorname());
        assertTrue(veranstaltung.getTeilnehmer().contains(teilnehmer));
    }

    @Test
    void teilnehmerNameValidierung() {

        Teilnehmer validTeilnehmer = new Teilnehmer(12325198L, "Hans-Peter", "Müller");
        assertDoesNotThrow(() -> teilnehmerRepository.save(validTeilnehmer));


        Teilnehmer invalidTeilnehmer = new Teilnehmer(123L, "Hans123", "Müller!");
        Exception exception = assertThrows(TransactionSystemException.class, () ->
                teilnehmerRepository.save(invalidTeilnehmer)
        );


        assertTrue(exception.getCause().getCause().getMessage().contains("Ziffern"));
    }


    @Test
    void teilnehmerVonVeranstaltungEntfernen() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testUser", "Max", "Mustermann", "password", Benutzer.Role.ADMIN));
        Veranstaltung veranstaltung = veranstaltungRepository.save(new Veranstaltung("Netzwerke", "Wintersemester 2025", benutzer));
        Teilnehmer teilnehmer = teilnehmerRepository.save(new Teilnehmer(33245334L, "Maxi", "Test"));
        veranstaltung.addTeilnehmer(teilnehmer);
        veranstaltungRepository.save(veranstaltung);

        veranstaltung.removeTeilnehmer(teilnehmer);
        veranstaltungRepository.save(veranstaltung);

        System.out.println("Teilnehmer erfolgreich entfernt: " + teilnehmer.getVorname() + " " + teilnehmer.getNachname());
        assertFalse(veranstaltung.getTeilnehmer().contains(teilnehmer));
    }

    @Test
    void erfolgreichTerminZuVeranstaltungHinzufuegen() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testUser", "Max", "Mustermann", "password", Benutzer.Role.ADMIN));
        Veranstaltung veranstaltung = veranstaltungRepository.save(new Veranstaltung("Chemie", "Wintersemester 2025", benutzer));
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(14, 0), LocalTime.of(16, 0));
        veranstaltung.addTermin(termin);
        veranstaltungRepository.save(veranstaltung);

        System.out.println("Termin erfolgreich hinzugefügt: " + termin.getDatum() + ", " + termin.getStartzeit());
        assertEquals(1, veranstaltung.getTermine().size());
    }

    @Test
    void entferneTerminFromVeranstaltung() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testUser", "Max", "Mustermann", "password", Benutzer.Role.ADMIN));
        Veranstaltung veranstaltung = veranstaltungRepository.save(new Veranstaltung("Biologie", "Wintersemester 2025", benutzer));
        Termin termin = terminRepository.save(new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        veranstaltung.addTermin(termin);
        veranstaltungRepository.save(veranstaltung);

        veranstaltung.removeTermin(termin);
        veranstaltungRepository.save(veranstaltung);

        System.out.println("Termin erfolgreich entfernt.");
        assertEquals(0, veranstaltung.getTermine().size());
    }

    @Test
    void terminMitUngueltigerZeit() {
        Benutzer benutzer = benutzerRepository.save(new Benutzer("testUser", "Test", "User", "pwd", Benutzer.Role.ADMIN));
        Veranstaltung veranstaltung = veranstaltungRepository.save(new Veranstaltung("TestVeranstaltung", "Wintersemester 2025", benutzer));

        Termin termin = new Termin(
                LocalDate.now(),
                LocalTime.of(16, 0), // Endzeit vor Startzeit
                LocalTime.of(14, 0)
        );
        termin.setVeranstaltung(veranstaltung);

        Exception exception = assertThrows(Exception.class, () -> {
            terminRepository.save(termin);
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Die Startzeit muss vor der Endzeit liegen"));
    }


    @Test
    void addGruppenarbeitSuccessfully() {
        Termin termin = terminRepository.save(new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Präsentation", termin);
        gruppenarbeitRepository.save(gruppenarbeit);

        System.out.println("Gruppenarbeit erfolgreich hinzugefügt: " + gruppenarbeit.getTitel());
        assertTrue(gruppenarbeitRepository.existsById(gruppenarbeit.getId()));
    }

    @Test
    void veranstaltungMitTerminGruppearbeitLoeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed


        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);

        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));

        // Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Löschen
        veranstaltungService.deleteVeranstaltungMitAbhaengigkeiten(veranstaltungService.findVeranstaltungById(veranstaltung.getId()));

        if (veranstaltungService.findVeranstaltungById(veranstaltung.getId())==null)
        {
            System.out.println("Veranstaltung gelöscht: " + veranstaltung.getTitel());
        }
        else
        {
            System.out.println("Im Test: Veranstaltung konnte nicht gelöscht werden: " + veranstaltung.getTitel());
        }

        assertTrue(gruppenarbeitRepository.findById(gruppenarbeit.getId()).isEmpty());
        assertFalse(veranstaltungRepository.existsById(veranstaltung.getId()));
        assertFalse(terminRepository.existsById(termin.getId()));


        System.out.println("Veranstaltung + abhängige Entitäten erfolgreich gelöscht");
    }


    @Test
    void veranstaltungMitTerminGruppearbeitGruppenLoeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        //Termin erstellen
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);

        //Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));



        //Gruppen anlegen
        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);


         //Veranstaltung löschen
        veranstaltungService.deleteVeranstaltungMitAbhaengigkeiten(veranstaltungService.findVeranstaltungById(veranstaltung.getId()));

        if (veranstaltungService.findVeranstaltungById(veranstaltung.getId())==null)
        {
            System.out.println("Veranstaltung gelöscht: " + veranstaltung.getTitel());
        }
        else
        {
            System.out.println("Im Test: Veranstaltung konnte nicht gelöscht werden: " + veranstaltung.getTitel());
        }
        // 🔹 Assertions
        assertTrue(gruppenarbeitRepository.findById(gruppenarbeit.getId()).isEmpty());
        assertFalse(veranstaltungRepository.existsById(veranstaltung.getId()));
        assertFalse(terminRepository.existsById(termin.getId()));


        System.out.println("Veranstaltung + abhängige Entitäten erfolgreich gelöscht");
    }

    @Test
    void teilnahmeloeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);
        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));


        // Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Gruppen anlegen
        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);
        List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(gruppenarbeit);
        assertFalse(teilnahmen.isEmpty());

        teilnahmeService.deleteAllByGruppenarbeit(gruppenarbeit);
        List<Teilnahme> nachDemLoeschen = teilnahmeService.findeAlleByGruppenarbeit(gruppenarbeit);
        assertEquals(0, nachDemLoeschen.size(), "Es sollten 0 Teilnahmen nach dem Löschen sein");

    }

    @Test
    void gruppeLoeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        //Termin erstellen
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);


        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));


        // Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Gruppen anlegen
        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);
        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        assertFalse(gruppen.isEmpty());

        gruppeService.deleteAllByGruppenarbeitmitTeilnahmen(gruppenarbeit);
        List<Gruppe> nachDemLoeschen = gruppeService.findeAlleByGruppenarbeit(gruppenarbeit);
        assertEquals(0, nachDemLoeschen.size(), "Es sollten 0 Gruppen nach dem Löschen sein");
    }

    @Test
    void gruppenarbeitLoeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        //Termin erstellen
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);

        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));


        // Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Gruppen anlegen
        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);
        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        assertFalse(gruppen.isEmpty());

        //Gruppenarbeit löschen
        gruppenarbeitService.deleteGruppenarbeitMitAbhaengigkeiten(gruppenarbeit);
        List<Gruppenarbeit> nachDemLoeschen = gruppenarbeitService.findeAlleGruppenarbeitenByTermin(termin);
        assertEquals(0.0, nachDemLoeschen.size(), "Es sollten 0 Gruppenarbeiten nach dem Löschen sein");
    }

    @Test
    void terminLoeschen() {

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        //Termin erstellen
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);

        veranstaltung = veranstaltungRepository.save(veranstaltung);


        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10045678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20023456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));


        // Gruppenarbeit erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        //Gruppen anlegen
        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);
        List<Gruppe> gruppen = gruppeRepository.findByGruppenarbeit(gruppenarbeit);
        assertFalse(gruppen.isEmpty());

        //Termin löschen
        terminService.deleteTerminMitAbhaengigkeiten(termin);
        List<Termin> nachDemLoeschen = terminService.findeAlleTermineByVeranstaltung(veranstaltung);
        assertEquals(0, nachDemLoeschen.size(), "Es sollten 0 Termine nach dem Löschen sein");
    }

    @Test
    void loescheVeranstaltungSoftwareprojekt(){
        //finde Veranstaltung mit Titel "Softwareprojekt"
        Veranstaltung veranstaltung = veranstaltungRepository.findByTitel("Softwareprojekt").orElse(null);
        if (veranstaltung != null) {
            //Lösche Veranstaltung
            veranstaltungService.deleteVeranstaltungMitAbhaengigkeiten(veranstaltung);
            System.out.println("Veranstaltung Softwareprojekt erfolgreich gelöscht.");
            assertFalse(veranstaltungRepository.existsById(veranstaltung.getId()));
        } else {
            System.out.println("Veranstaltung Softwareprojekt nicht gefunden.");
            fail("Veranstaltung Softwareprojekt sollte existieren.");
        }

    }

    @Test
    void loescheTeilnehmer(){

        Benutzer benutzer = benutzerRepository.save(
                new Benutzer("deleter", "Delete", "Tester", "pw", Benutzer.Role.ADMIN)
        );
        Veranstaltung veranstaltung = new Veranstaltung("Test-Löschung", "Wintersemester 2025", benutzer);
        veranstaltung = veranstaltungRepository.save(veranstaltung); // managed

        //Termin erstellen
        Termin termin = new Termin(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        veranstaltung.addTermin(termin);
        termin = terminRepository.save(termin);
        veranstaltung = veranstaltungRepository.save(veranstaltung);


        //Teilnehmer anlegen
        Teilnehmer teilnehmer1 = teilnehmerService.findOrCreateTeilnehmer(10005678L, "Hans", "Müller");
        Teilnehmer teilnehmer2 = teilnehmerService.findOrCreateTeilnehmer(20003456L, "Erika", "Mustermann");

        //Teilnehmer zu Veranstaltung hinzufügen
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer1, teilnehmer2));
        //Teilnehmer zu Termin hinzufügen
        terminService.addTeilnehmerToTermin(termin, List.of(teilnehmer1, teilnehmer2));

        //Gruppenarbeit  erstellen
        Gruppenarbeit gruppenarbeit = new Gruppenarbeit("Abgabe", termin);
        gruppenarbeit = gruppenarbeitRepository.save(gruppenarbeit);

        gruppenarbeitService.erstelleGruppenNachAnzahl(gruppenarbeit, 2);

        // Teilnehmer löschen
        teilnehmerService.deleteTeilnehmerMitAbhaengigkeiten(teilnehmer1);
        System.out.println("Teilnehmer erfolgreich gelöscht: " + teilnehmer1.getVorname() + " " + teilnehmer1.getNachname());


        assertFalse(teilnehmerRepository.existsById(teilnehmer1.getMatrnr()), "Teilnehmer sollte nicht mehr existieren");
    }



}


