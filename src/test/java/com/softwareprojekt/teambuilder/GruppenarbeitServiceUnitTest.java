package com.softwareprojekt.teambuilder;

import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//Author: Thenujan Karunakumar
@ExtendWith(MockitoExtension.class)
class GruppenarbeitServiceUnitTest {

    @InjectMocks
    private GruppenarbeitService gruppenarbeitService;

    @Test
    void testGueltigeTeilnehmerAnzahl_10() {
        List<Integer> result = gruppenarbeitService.berechneSinnvolleGruppenzahlen(10);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(2));
        assertTrue(result.contains(5));
    }

    @Test
    void testTeilnehmerAnzahl_1() {
        List<Integer> result = gruppenarbeitService.berechneSinnvolleGruppenzahlen(1);
        assertEquals(List.of(1), result);
    }

    @Test
    void testTeilnehmerAnzahl_0() {
        List<Integer> result = gruppenarbeitService.berechneSinnvolleGruppenzahlen(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTeilnehmerAnzahl_Negativ() {
        List<Integer> result = gruppenarbeitService.berechneSinnvolleGruppenzahlen(-3);
        assertTrue(result.isEmpty());
    }

}
