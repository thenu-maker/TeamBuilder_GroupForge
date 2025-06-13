package com.softwareprojekt.teambuilder;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.repository.TeilnehmerRepository;
import com.softwareprojekt.teambuilder.repository.TerminRepository;
import com.softwareprojekt.teambuilder.repository.VeranstaltungRepository;
import com.softwareprojekt.teambuilder.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

//Author: Fiona Sander
@Component
@Profile("dev")
public class Initialise implements ApplicationListener<ApplicationReadyEvent> {


    @Autowired
    private BenutzerService benutzerService;

    @Autowired
    private TeilnehmerService teilnehmerService;
    @Autowired
    private TerminService terminService;
    @Autowired
    private VeranstaltungService veranstaltungService;
    @Autowired
    private GruppenarbeitService gruppenarbeitService;
    @Autowired
    private GruppeService gruppeService;
    @Autowired
    private TeilnahmeService teilnahmeService ;


    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TeilnehmerRepository teilnehmerRepository;
    @Autowired
    private TerminRepository terminRepository;
    @Autowired
    private VeranstaltungRepository veranstaltungRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent){
        dbInitalizer();
    }

    private void dbInitalizer() {


        //region Benutzer
        if (benutzerService.findBenutzerByUsername("afoerster") == null) {
            Benutzer prof = benutzerService.createBenutzer("afoerster", "Alexander", "Förster", "changeMe", Benutzer.Role.PROFESSOR);
            prof.setTitel("Dr.");
            benutzerService.saveBenutzer(prof);
        }
        //endregion

        //region Teilnehmer
        Teilnehmer t1 = teilnehmerService.findOrCreateTeilnehmer(12345678L, "Hans", "Müller");
        Teilnehmer t2 = teilnehmerService.findOrCreateTeilnehmer(23456781L, "Peter", "Meier");
        Teilnehmer t3 = teilnehmerService.findOrCreateTeilnehmer(34567812L, "Dieter", "Schulz");
        Teilnehmer t4 = teilnehmerService.findOrCreateTeilnehmer(45678123L, "Barbara", "Fischer");
        Teilnehmer t5 = teilnehmerService.findOrCreateTeilnehmer(56781234L, "Sabine", "Wächter");
        Teilnehmer t6 = teilnehmerService.findOrCreateTeilnehmer(67812345L, "Hilde", "Meyer");
        Teilnehmer t7 = teilnehmerService.findOrCreateTeilnehmer(78123456L, "Johann", "Becker");
        Teilnehmer t8 = teilnehmerService.findOrCreateTeilnehmer(81234567L, "Mathias", "Weber");
        Teilnehmer t9 = teilnehmerService.findOrCreateTeilnehmer(91234567L, "Andreas", "Wagner");
        Teilnehmer t10 = teilnehmerService.findOrCreateTeilnehmer(10123456L, "Petra", "Hofmann");
        Teilnehmer t11 = teilnehmerService.findOrCreateTeilnehmer(11123456L, "Margarethe", "Schneider");
        Teilnehmer t12 = teilnehmerService.findOrCreateTeilnehmer(12234567L, "Lisbeth", "Schmidt");
        Teilnehmer t13 = teilnehmerService.findOrCreateTeilnehmer(13234567L, "Klaus", "Zimmermann");
        Teilnehmer t14 = teilnehmerService.findOrCreateTeilnehmer(14345678L, "Erika", "Krüger");
        Teilnehmer t15 = teilnehmerService.findOrCreateTeilnehmer(15456789L, "Thomas", "Bauer");
        Teilnehmer t16 = teilnehmerService.findOrCreateTeilnehmer(16567890L, "Anja", "Koch");
        Teilnehmer t17 = teilnehmerService.findOrCreateTeilnehmer(17678901L, "Michael", "Richter");
        Teilnehmer t18 = teilnehmerService.findOrCreateTeilnehmer(18789012L, "Ursula", "Graf");
        Teilnehmer t19 = teilnehmerService.findOrCreateTeilnehmer(19890123L, "Helmut", "Jäger");
        Teilnehmer t20 = teilnehmerService.findOrCreateTeilnehmer(20901234L, "Ingrid", "Hartmann");
        Teilnehmer t21 = teilnehmerService.findOrCreateTeilnehmer(21012345L, "Stefan", "Lorenz");
        Teilnehmer t22 = teilnehmerService.findOrCreateTeilnehmer(22123456L, "Gisela", "Schäfer");
        Teilnehmer t23 = teilnehmerService.findOrCreateTeilnehmer(23234567L, "Rolf", "Brandt");
        Teilnehmer t24 = teilnehmerService.findOrCreateTeilnehmer(24345678L, "Heike", "Arnold");

        //endregion

        //region Veranstaltung
        Benutzer prof = benutzerService.findBenutzerByUsername("afoerster");

        Veranstaltung v1 = veranstaltungService.findOrCreateVeranstaltung("Softwareprojekt", "Wintersemester 2025", prof.getId());
        Veranstaltung v2 = veranstaltungService.findOrCreateVeranstaltung("Webtechnologien", "Sommersemester 2025", prof.getId());
        Veranstaltung v3 = veranstaltungService.findOrCreateVeranstaltung("Mathe", "Wintersemester 2025", prof.getId());
        Veranstaltung v4 = veranstaltungService.findOrCreateVeranstaltung("Datenbanken", "Wintersemester 2025", prof.getId());
        //endregion

        //region Termin
        Termin termin1 = terminService.findOrCreateTermin(LocalDate.of(2025,1,6), LocalTime.of(9,45), LocalTime.of(11,15), v1);
        Termin termin2 = terminService.findOrCreateTermin(LocalDate.of(2025,12,26), LocalTime.of(11,30), LocalTime.of(13,0), v1);
        Termin termin3 = terminService.findOrCreateTermin(LocalDate.of(2025,8,12), LocalTime.of(14,0), LocalTime.of(15,30), v2);
        Termin termin4 = terminService.findOrCreateTermin(LocalDate.of(2026,8,14), LocalTime.of(9,45), LocalTime.of(11,15), v3);
        Termin termin5 = terminService.findOrCreateTermin(LocalDate.of(2025,10,2), LocalTime.of(14,0), LocalTime.of(15,30), v4);
        Termin termin6 = terminService.findOrCreateTermin(LocalDate.of(2025,9,17), LocalTime.of(14,0), LocalTime.of(15,30), v4);
        Termin termin7 = terminService.findOrCreateTermin(LocalDate.of(2026,8,12), LocalTime.of(8,0), LocalTime.of(9,45), v3);
        Termin termin8 = terminService.findOrCreateTermin(LocalDate.of(2025,8,12), LocalTime.of(14,0), LocalTime.of(15,30), v2);
        Termin termin9 = terminService.findOrCreateTermin(LocalDate.of(2026,8,12), LocalTime.of(9,45), LocalTime.of(11,15), v3);
        //endregion

        //region Teilnehmer_Termin
        terminService.addTeilnehmerToTermin(termin1, List.of(t1, t2, t3, t4, t5));
        terminService.addTeilnehmerToTermin(termin2, List.of(t1, t2, t3, t4, t5));
        terminService.addTeilnehmerToTermin(termin3, List.of(t10, t11, t12, t1, t2, t3));
        terminService.addTeilnehmerToTermin(termin4, List.of(t11, t12, t7, t8, t9, t3));
        terminService.addTeilnehmerToTermin(termin5, List.of(t10, t1, t7, t4, t5, t12));
        terminService.addTeilnehmerToTermin(termin6, List.of(t10, t1, t7, t4, t5, t12));
        terminService.addTeilnehmerToTermin(termin7, List.of(t11, t12, t7, t8, t9, t3));
        terminService.addTeilnehmerToTermin(termin8, List.of(t10, t11, t12, t1, t2, t3));
        terminService.addTeilnehmerToTermin(termin9, List.of(t11, t12, t7, t8, t9, t3));
        //endregion

        //region Teilnehmer_Veranstaltung
        veranstaltungService.addTeilnehmerToVeranstaltung(v1, List.of(t1, t2, t3, t4, t5));
        veranstaltungService.addTeilnehmerToVeranstaltung(v2, List.of(t10, t11, t12, t1, t2, t3));
        veranstaltungService.addTeilnehmerToVeranstaltung(v3, List.of(t11, t12, t7, t8, t9, t3));
        veranstaltungService.addTeilnehmerToVeranstaltung(v4, List.of(t10, t1, t7, t4, t5, t12));
        //endregion

        Gruppenarbeit ga1 = gruppenarbeitService.findOrCreateGruppenarbeit("Prototyping", termin1);
        Gruppenarbeit ga2 = gruppenarbeitService.findOrCreateGruppenarbeit("Erstellen", termin1);
        Gruppenarbeit ga3 = gruppenarbeitService.findOrCreateGruppenarbeit("Initialisieren", termin9);
        Gruppenarbeit ga4 = gruppenarbeitService.findOrCreateGruppenarbeit("Datenbankzugang", termin5);

// Erzeuge je Gruppenarbeit mehrere Gruppen und Teilnahmen
        createGruppenUndTeilnahmen(ga1, termin1, 2);
        createGruppenUndTeilnahmen(ga2, termin1, 3);
        createGruppenUndTeilnahmen(ga3, termin9, 2);
        createGruppenUndTeilnahmen(ga4, termin5, 2);





    }

    private void createGruppenUndTeilnahmen(Gruppenarbeit gruppenarbeit, Termin termin, int anzahlGruppen) {
        List<Teilnehmer> teilnehmerList = new ArrayList<>(termin.getTeilnehmer());
        List<Gruppe> gruppen = new ArrayList<>();

        // Gruppen erzeugen
        for (int i = 1; i <= anzahlGruppen; i++) {
            Gruppe gruppe = gruppeService.findOrCreateGruppe("Gruppe " + i, gruppenarbeit);
            //Eine Liste mit zufälligen Anmerkungen für die Gruppen
            String[] anmerkungen = {
                    "Thema A",
                    "Thema B",
                    "Thema C",
                    "Thema D",
                    "Thema E",
                    "Thema F",
                    "Thema G",
            };
            //Zufällige Anmerkung auswählen
            String anmerkung = anmerkungen[(int) (Math.random() * anmerkungen.length)];
            gruppe.setAnmerkung(anmerkung);
            gruppen.add(gruppe);
            gruppeService.saveGruppe(gruppe);
        }

        // Teilnehmer gleichmäßig auf Gruppen verteilen
        for (int i = 0; i < teilnehmerList.size(); i++) {
            Teilnehmer teilnehmer = teilnehmerList.get(i);
            Gruppe zielGruppe = gruppen.get(i % anzahlGruppen); // Rundlaufverteilung

            int lp = 5 + (int) (Math.random() * 6); // Ganzzahlig zwischen 5 und 10
            int pp = 3 + (int) (Math.random() * 8); // Ganzzahlig zwischen 3 und 10

            teilnahmeService.createTeilnahme(zielGruppe, teilnehmer, (double) lp, (double) pp);
        }
    }


}
