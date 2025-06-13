package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Termin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Author: Thenujan Karunakumar
public interface GruppenarbeitRepository extends JpaRepository<Gruppenarbeit, Long> {

    Gruppenarbeit findByTitel(String titel);

    Gruppenarbeit findByTitelAndTermin(String titel, Termin termin);

    List<Gruppenarbeit> findByTermin(Termin termin);

    Gruppenarbeit findByTitelAndTerminId(String titel, long l);
    List<Gruppenarbeit> findByTermin_Veranstaltung_Id(Long veranstaltungId);
}
