package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

//Author: Fiona Sander
@Entity
public class Gruppenarbeit {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @NotBlank(message = "Bitte geben Sie einen Titel ein.")
    private String titel;

    @OneToMany(mappedBy = "gruppenarbeit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gruppe> gruppen = new ArrayList<>();

    @ManyToOne
    @JoinColumn
    private Termin termin;

    public Gruppenarbeit() {}

    public Gruppenarbeit(String titel) {
        this.titel = titel;
    }


    public Gruppenarbeit(String titel, Termin termin) {
        this.titel = titel;
        this.termin = termin;
    }

    public long getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public Termin getTermin() {
        return termin;
    }

    public void setTermin(Termin termin) {
        this.termin = termin;
    }

    public List<Gruppe> getGruppen() {
        return gruppen;
    }

    public void addGruppe(Gruppe gruppe) {
        if (!gruppen.contains(gruppe)) {
            gruppen.add(gruppe);
            gruppe.setGruppenarbeit(this);
        }
    }

    public void removeGruppe(Gruppe gruppe) {
        if (gruppen.remove(gruppe)) {
            gruppe.setGruppenarbeit(null);
        }
    }

    public void deleteAllGruppen() {
        for (Gruppe gruppe : new ArrayList<>(gruppen)) {
            removeGruppe(gruppe);
        }
    }
}