package com.softwareprojekt.teambuilder.entities;

import jakarta.persistence.*;

//Author: Thenujan Karunakumar
@Entity
public class Teilnahme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; //unsicher

    private double leistungspunkte;

    private double praesentationspunkte;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gruppe_id")
    private Gruppe gruppe;

    @ManyToOne
    @JoinColumn(name = "teilnehmer_id")
    private Teilnehmer teilnehmer;

    public Teilnahme() {
    }

    public Teilnahme(Gruppe gruppe, Teilnehmer teilnehmer) {

        this.gruppe = gruppe;
        this.teilnehmer = teilnehmer;
    }

    public long getId() {
        return id;
    }

    public double getPraesentationspunkte() {
        return praesentationspunkte;
    }

    public void setPraesentationspunkte(double praesentationspunkte) {
        this.praesentationspunkte = praesentationspunkte;
    }

    public double getLeistungspunkte() {
        return leistungspunkte;
    }

    public void setLeistungspunkte(double leistungspunkte) {
        this.leistungspunkte = leistungspunkte;
    }

    public Gruppe getGruppe() {
        return gruppe;
    }

    public void setGruppe(Gruppe gruppe) {
        this.gruppe = gruppe;
    }

    public Teilnehmer getTeilnehmer() {
        return teilnehmer;
    }

    public void setTeilnehmer(Teilnehmer teilnehmer) {
        this.teilnehmer = teilnehmer;
    }
}
