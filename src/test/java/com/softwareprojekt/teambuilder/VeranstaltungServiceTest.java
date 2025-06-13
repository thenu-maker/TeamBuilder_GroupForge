
package com.softwareprojekt.teambuilder;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.repository.BenutzerRepository;
import com.softwareprojekt.teambuilder.repository.TeilnehmerRepository;
import com.softwareprojekt.teambuilder.repository.VeranstaltungRepository;
import com.softwareprojekt.teambuilder.services.VeranstaltungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
//Author: Thenujan Karunakumar
public class VeranstaltungServiceTest {

    @Mock
    private VeranstaltungRepository veranstaltungRepository;

    @Mock
    private TeilnehmerRepository teilnehmerRepository;

    @Mock
    private BenutzerRepository benutzerRepository;

    @InjectMocks
    private VeranstaltungService veranstaltungService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddTeilnehmerToVeranstaltung_validInput() {
        Veranstaltung veranstaltung = new Veranstaltung();
        Teilnehmer teilnehmer = new Teilnehmer(12345678L, "Hans", "Müller");

        when(teilnehmerRepository.findById(teilnehmer.getMatrnr())).thenReturn(Optional.of(teilnehmer));

        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of(teilnehmer));

        assertTrue(veranstaltung.getTeilnehmer().contains(teilnehmer));
    }

    @Test
    void testFindVeranstaltungById_found() {
        Veranstaltung veranstaltung = new Veranstaltung();

        when(veranstaltungRepository.findById(veranstaltung.getId())).thenReturn(Optional.of(veranstaltung));

        Veranstaltung found = veranstaltungService.findVeranstaltungById(veranstaltung.getId());

        assertEquals(veranstaltung.getId(), found.getId());
    }

    @Test
    void testFindVeranstaltungById_notFound() {
        when(veranstaltungRepository.findById(2000000L)).thenReturn(Optional.empty());
        assertNull(veranstaltungService.findVeranstaltungById(2000000L));
    }

    @Test
    void testFindVeranstaltungByTitel_existing() {
        Veranstaltung veranstaltung = new Veranstaltung("Programmieren", "SS25", new Benutzer());
        when(veranstaltungRepository.findByTitel("Programmieren")).thenReturn(Optional.of(veranstaltung));

        Optional<Veranstaltung> result = Optional.ofNullable(veranstaltungService.findVeranstaltungByTitel("Programmieren"));

        assertTrue(result.isPresent());
        assertEquals("Programmieren", result.get().getTitel());
    }

    @Test
    void testAddTeilnehmerToVeranstaltung_leereTeilnehmerliste() {
        Veranstaltung veranstaltung = new Veranstaltung();
        veranstaltungService.addTeilnehmerToVeranstaltung(veranstaltung, List.of());
        assertTrue(veranstaltung.getTeilnehmer().isEmpty(), "Bei leerer Teilnehmerliste sollten keine Teilnehmer hinzugefügt werden");
    }

    @Test
    void testFindVeranstaltungByTitel_notExisting() {
        when(veranstaltungRepository.findByTitel("NichtVorhanden")).thenReturn(Optional.empty());

        Veranstaltung result = veranstaltungService.findVeranstaltungByTitel("NichtVorhanden");

        assertNull(result, "Nicht vorhandene Veranstaltung sollte null zurückgeben");
    }

    @Test
    void testFindVeranstaltungByTitel_nullTitel() {
        when(veranstaltungRepository.findByTitel(null)).thenReturn(Optional.empty());

        Veranstaltung result = veranstaltungService.findVeranstaltungByTitel(null);

        assertNull(result, "Suche mit null-Titel sollte null zurückgeben");
    }

}
