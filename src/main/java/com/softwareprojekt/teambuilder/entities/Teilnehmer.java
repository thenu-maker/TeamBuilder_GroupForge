package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.*;

//Author: Tolga Cenk Kilic
@Entity
public class Teilnehmer {

    //Attribute
    @Id
    @NotNull(message = "Bitte geben Sie eine Matrikelnummer ein.")
    @Min(value = 10000000, message = "Die Matrikelnummer muss 8 Ziffern beinhalten")
    @Max(value = 99999999, message = "Die Matrikelnummer muss 8 Ziffern beinhalten")
    private Long matrnr;

    @NotNull(message = "Bitte geben Sie einen Vornamen ein.")
    @Pattern(regexp = "[\\p{L}\\- ]+",
            message = "Der Vorname darf nur Buchstaben enthalten")
    private String vorname;

    @NotNull(message = "Bitte geben Sie einen Nachnamen ein.")
    @Pattern(regexp = "[\\p{L}\\- ]+",
            message = "Der Nachname darf nur Buchstaben enthalten")
    private String nachname;

    //Beziehung zu der Veranstaltungstabelle (m:n)
    @ManyToMany (fetch = FetchType.EAGER, mappedBy = "teilnehmer", cascade = CascadeType.PERSIST)
    private Set<Veranstaltung> veranstaltungen= new HashSet<>();


    @ManyToMany(mappedBy = "teilnehmer", cascade = CascadeType.PERSIST,  fetch = FetchType.EAGER)
    private Set<Termin> termine= new HashSet<>();


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "teilnehmer", cascade = CascadeType.ALL,orphanRemoval = true )
    private List<Teilnahme> teilnahmen = new ArrayList<>();

    //Konstruktoren
    public Teilnehmer(){}

    public Teilnehmer(long matrnr){
        this.matrnr = matrnr;
    }

    public Teilnehmer(long matrnr, String vorname, String nachname){
        this.matrnr = matrnr;
        this.vorname= vorname;
        this.nachname= nachname;
    }

    //Getter + Setter

    public long getMatrnr() {
        return matrnr;
    }

    public void setMatrnr(long matrnr) {
        this.matrnr = matrnr;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public void addVeranstaltung(Veranstaltung veranstaltung){
        veranstaltungen.add(veranstaltung);
    }

    public void removeVeranstaltung(Veranstaltung veranstaltung){
        veranstaltungen.remove(veranstaltung);
    }

    public List<Veranstaltung> getVeranstaltungen(){
        return veranstaltungen.stream().toList();
    }

    public void addTermin(Termin termin){
        termine.add(termin);
    }

    public void removeTermin(Termin termin){
        termine.remove(termin);
    }

    public List<Termin> getTermine(){
        return termine.stream().toList();
    }

    // Teilnehmer.java
    public void addTeilnahme(Teilnahme teilnahme) {
        if (!teilnahmen.contains(teilnahme)) {
            teilnahmen.add(teilnahme);
            teilnahme.setTeilnehmer(this);
        }
    }

    public void removeTeilnahme(Teilnahme teilnahme) {
        if (teilnahmen.remove(teilnahme)) {
            teilnahme.setTeilnehmer(null);
        }
    }




    public List<Teilnahme> getTeilnahmen(){
        return teilnahmen.stream().toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teilnehmer that = (Teilnehmer) o;
        return Objects.equals(matrnr, that.matrnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrnr);
    }

    @Override
    public String toString() {
        return "Teilnehmer{" +
                "matrnr=" + matrnr +
                ", vorname='" + vorname + '\'' +
                ", nachname='" + nachname + '\'' +
                '}';
    }
}
