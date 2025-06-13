package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//Author: Silas Weber
public interface VeranstaltungRepository extends JpaRepository<Veranstaltung, Long> {

    Optional<Veranstaltung> findByTitel(String titel);

    @Query("select v from Veranstaltung v " +
            "where lower(v.titel) like lower(concat('%', :searchTerm, '%')) "+
            "or lower(v.semester) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(v.benutzer.username) like lower(concat('%', :searchTerm, '%')) ")
    List<Veranstaltung> search(@Param("searchTerm") String searchTerm);
}
