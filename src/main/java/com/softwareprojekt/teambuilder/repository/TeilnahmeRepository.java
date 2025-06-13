package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Author: Thenujan Karunakumar
public interface TeilnahmeRepository extends JpaRepository<Teilnahme, Long> {
    List<Teilnahme> findByGruppeIn(List<Gruppe> alteGruppen);

    List<Teilnahme> findAllByGruppe_Gruppenarbeit(Gruppenarbeit aktuelleGruppenarbeit);
    Optional<Teilnahme> findByGruppeAndTeilnehmer(Gruppe gruppe, Teilnehmer teilnehmer);

    boolean existsByGruppe_Gruppenarbeit(Gruppenarbeit selected);

    List<Teilnahme> findAllByGruppe(Gruppe gruppe);

    Teilnahme findById(long id);

    void deleteAllByTeilnehmer(Teilnehmer teilnehmer);

    List<Teilnahme> findAllByTeilnehmer(Teilnehmer teilnehmer);
}
