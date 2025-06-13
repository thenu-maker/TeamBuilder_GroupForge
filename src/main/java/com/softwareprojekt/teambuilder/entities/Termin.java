package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Author: Tolga Cenk Kilic
@Entity
public class Termin {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @NotNull
    private LocalDate datum;

    @NotNull
    //Attribut für die Uhrzeit ergänzt
    private LocalTime startzeit;

    @NotNull
    private LocalTime endzeit;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "termin")
    private List<Gruppenarbeit> gruppenarbeiten = new ArrayList<Gruppenarbeit>();

    @ManyToMany(cascade =CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Teilnehmer> teilnehmer = new ArrayList<Teilnehmer>();

    @ManyToOne
    @JoinColumn(name = "veranstaltung_id")
    private Veranstaltung veranstaltung;

    public Termin() {
    }

    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (startzeit != null && endzeit != null) {
            if (startzeit.isAfter(endzeit) || startzeit.equals(endzeit)) {
                throw new IllegalArgumentException("Die Startzeit muss vor der Endzeit liegen");
            }
        }
    }

    public Termin(LocalDate datum, List<Teilnehmer> teilnehmer) {
        this.datum = datum;
        this.teilnehmer = teilnehmer;
    }


    public Termin(LocalDate datum, LocalTime startzeit, LocalTime endzeit, Veranstaltung veranstaltung) {
        this.datum = datum;
        this.startzeit = startzeit;
        this.endzeit = endzeit;
        this.veranstaltung = veranstaltung;
    }


    public Termin(LocalDate datum, LocalTime startzeit, LocalTime endzeit) {
        this.datum = datum;
        this.startzeit = startzeit;
        this.endzeit = endzeit;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getStartzeit() {
        return startzeit;
    }

    public void setStartzeit(LocalTime startzeit) {
        this.startzeit = startzeit;
    }

    public LocalTime getEndzeit() {
        return endzeit;
    }

    public void setEndzeit(LocalTime endzeit) {
        this.endzeit = endzeit;
    }

    public List<Gruppenarbeit> getGruppenarbeiten() {
        return gruppenarbeiten;
    }

    public void setGruppenarbeiten(List<Gruppenarbeit> gruppenarbeiten) {
        this.gruppenarbeiten = gruppenarbeiten;
    }



    public void addGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        if (!gruppenarbeiten.contains(gruppenarbeit)) {
            gruppenarbeiten.add(gruppenarbeit);
            gruppenarbeit.setTermin(this);
        }
    }

    public void removeGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        if (gruppenarbeiten.remove(gruppenarbeit)) {
            gruppenarbeit.setTermin(null);
        }
    }

    public List<Teilnehmer> getTeilnehmer() {
        return teilnehmer.stream().toList();
    }

    public void setTeilnehmer(List<Teilnehmer> teilnehmer) {
        this.teilnehmer = teilnehmer;
    }



    public Veranstaltung getVeranstaltung() { return veranstaltung; }

    public void setVeranstaltung(Veranstaltung veranstaltung) {this.veranstaltung = veranstaltung; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Termin termin = (Termin) o;
        return Objects.equals(datum, termin.datum) &&
                Objects.equals(startzeit, termin.startzeit) &&
                Objects.equals(endzeit, termin.endzeit);
    }

    public boolean hatTeilnehmer(Teilnehmer teilnehmer) {
        return this.teilnehmer.contains(teilnehmer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datum, startzeit, endzeit);
    }


    public void addTeilnehmer(Teilnehmer t) {
        if (!teilnehmer.contains(t)) {
            teilnehmer.add(t);
            t.addTermin(this);
        }
    }

    public void removeTeilnehmer(Teilnehmer t) {
        teilnehmer.remove(t);
        t.removeTermin(this);
    }

    public void clearTeilnehmer() {
        if (teilnehmer != null) {
            for (Teilnehmer t : new ArrayList<>(teilnehmer)) {
                t.removeTermin(this); // ← Referenz beim Teilnehmer entfernen!
            }
            teilnehmer.clear(); // ← danach Liste leeren
        }
    }
}
