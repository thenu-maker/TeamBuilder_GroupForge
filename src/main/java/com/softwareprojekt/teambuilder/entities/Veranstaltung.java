package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Author: Tolga Cenk Kilic

@Entity
public class Veranstaltung {

    //Attribute
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Bitte geben Sie einen Titel ein.")
    private String titel;

    @NotBlank(message = "Bitte wählen Sie ein Semester aus.")
    private String semester;

    @ManyToOne
    @JoinColumn(name = "benutzer_id")
    @NotNull(message = "Bitte wählen Sie einen Veranstalter aus")
    private Benutzer benutzer;

    //Beziehung zu der Teilnehmertabelle (m:n)
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<Teilnehmer> teilnehmer = new HashSet<>();


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "veranstaltung", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Termin> termine= new ArrayList<Termin>();


    //Obligatorischer leerer Konstruktor
    public Veranstaltung() {}

    public Veranstaltung(String titel, String semester) {
        this.titel = titel;
        this.semester = semester;
    }
    public Veranstaltung(String titel, String semester, Benutzer benutzer) {
        this.titel = titel;
        this.semester = semester;
        this.benutzer= benutzer;
    }

    public Veranstaltung(String titel) {
        this.titel = titel;
    }

    //Getter + Setter
    public long getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public List<Teilnehmer> getTeilnehmer() {

        return teilnehmer.stream().toList();
    }

    public void setTeilnehmer(Set<Teilnehmer> teilnehmer) {
        this.teilnehmer = teilnehmer;
    }


    public void addTeilnehmer(Teilnehmer t) {
        if (!teilnehmer.contains(t)) {
            teilnehmer.add(t);
            t.addVeranstaltung(this);
        }
    }

    public void removeTeilnehmer(Teilnehmer t) {
        teilnehmer.remove(t);
        t.removeVeranstaltung(this);
    }


    public void setTermine(List<Termin> termine) {
        this.termine = termine;
    }

    public List<Termin> getTermine() {
        termine.sort((t1, t2) -> t1.getDatum().compareTo(t2.getDatum()));
        return termine.stream().toList();
    }

    public void addTermin(Termin termin) {
        if (!termine.contains(termin)) {
            termine.add(termin);
            termin.setVeranstaltung(this);
        }
    }


    public void removeTermin(Termin termin) {
        if (termine.remove(termin)) {
            termin.setVeranstaltung(null);
        }
    }

    public void clearTeilnehmer() {
        if (teilnehmer != null) {
            for (Teilnehmer t : new ArrayList<>(teilnehmer)) {
                t.removeVeranstaltung(this);
            }
            teilnehmer.clear();
        }
    }



    public void setBenutzer(Benutzer benutzer) {
        this.benutzer = benutzer;
    }

    public Benutzer getBenutzer() {
        return benutzer;
    }

}
