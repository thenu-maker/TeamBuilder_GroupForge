package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

//Author: Thenujan Karunakumar
@Entity
public class Gruppe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String gruppenname;

    private String anmerkung;

    @ManyToOne
    @JoinColumn(name = "gruppenarbeit_id")
    private Gruppenarbeit gruppenarbeit;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "gruppe",
            cascade = CascadeType.ALL)
    private List<Teilnahme> teilnahmen = new ArrayList<>();

    public Gruppe(){}

    public Gruppe (String gruppenname, Gruppenarbeit gruppenarbeit) {
        this.gruppenname = gruppenname;
        this.gruppenarbeit = gruppenarbeit;
    }

    public long getId() {
        return id;
    }

    public String getAnmerkung() {
        return anmerkung;
    }

    public void setAnmerkung(String anmerkung) {
        this.anmerkung = anmerkung;
    }

    public String getGruppenname() {
        return gruppenname;
    }

    public void setGruppenname(String gruppenname) {
        this.gruppenname = gruppenname;
    }

    public Gruppenarbeit getGruppenarbeit() {
        return gruppenarbeit;
    }

    public void setGruppenarbeit(Gruppenarbeit gruppenarbeit) {
        this.gruppenarbeit = gruppenarbeit;
    }

    public void setTeilnahmen(List<Teilnahme> list) {teilnahmen = list;}


    public List<Teilnahme> getTeilnahmen() {return teilnahmen.stream().toList();}


    public void addTeilnahme(Teilnahme teilnahme) {
        if (!teilnahmen.contains(teilnahme)) {
            teilnahmen.add(teilnahme);
            teilnahme.setGruppe(this);
        }
    }

    public void removeTeilnahme(Teilnahme teilnahme) {
        if (teilnahmen.remove(teilnahme)) {
            teilnahme.setGruppe(null);
        }
    }

}