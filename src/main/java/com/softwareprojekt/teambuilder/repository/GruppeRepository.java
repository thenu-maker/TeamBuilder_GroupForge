package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//Author: Thenujan Karunakumar
public interface GruppeRepository extends JpaRepository<Gruppe, Long> {
    List<Gruppe> findByGruppenarbeit(Gruppenarbeit gruppenarbeit);

    Gruppe findByGruppennameAndGruppenarbeit(String gruppenname, Gruppenarbeit gruppenarbeit);

    List<Gruppe> findAllByGruppenarbeit(Gruppenarbeit gruppenarbeit);

    List<Gruppe> findAllByGruppenarbeitId(Long gruppenarbeitId);
}
