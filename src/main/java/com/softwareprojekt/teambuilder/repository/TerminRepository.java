package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

//Author: Tolga Cenk Kilic
public interface TerminRepository extends JpaRepository<Termin, Long> {
    Optional<Termin> findByDatumAndStartzeitAndVeranstaltung(LocalDate datum, LocalTime startzeit, Veranstaltung veranstaltung);

    @Override
    Optional<Termin> findById(Long aLong);
    List<Termin> findByVeranstaltung(Veranstaltung veranstaltung);

    boolean existsByDatumAndStartzeitAndEndzeit(
            LocalDate datum,
            LocalTime startzeit,
            LocalTime endzeit
    );

}
