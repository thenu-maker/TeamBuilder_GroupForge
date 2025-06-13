package com.softwareprojekt.teambuilder.repository;


import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
//Author: Silas Weber
public interface TeilnehmerRepository extends JpaRepository<Teilnehmer, Long> {

    Teilnehmer findTeilnehmerByMatrnr(long i);


    @Query("select t from Teilnehmer t " +
            "where lower(cast(t.matrnr as string)) like lower(concat('%', :searchTerm, '%')) "+
            "or lower(t.vorname) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(t.nachname) like lower(concat('%', :searchTerm, '%')) ")
    List<Teilnehmer> search(@Param("searchTerm") String searchTerm);

    List<Teilnehmer> findByVeranstaltungen_Id(long id);

    List<Teilnehmer> findByTermineIn(List<Termin> terminListe);
}
