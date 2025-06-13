package com.softwareprojekt.teambuilder.repository;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Author: Silas Weber
@Repository
public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {

    Benutzer findByUsername(String username);
    Optional<Benutzer> findById(Long id);

    @Query("select b from Benutzer b " +
            "where lower(b.username) like lower(concat('%', :searchTerm, '%')) "+
            "or lower(b.vorname) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(b.nachname) like lower(concat('%', :searchTerm, '%')) ")
    List<Benutzer> search(@Param("searchTerm") String searchTerm);

}
