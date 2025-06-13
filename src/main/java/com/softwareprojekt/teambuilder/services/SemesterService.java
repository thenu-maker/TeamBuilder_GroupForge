package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
//Author: Silas Weber
@Service
public class SemesterService {

    /**
     * Enum zur Repräsentation der Semesterarten (Sommer- und Wintersemester).
     */
    public enum SemesterEnum {
        Wintersemester,
        Sommersemester
    }

    /**
     * Standard-Konstruktor für den {@code SemesterService}.
     */

    @Autowired
    private VeranstaltungService veranstaltungService;

    public SemesterService() {

    }

    /**
     * Gibt eine Liste der aktuellen, vorherigen und nächsten Semester als Strings zurück.
     * Die Liste enthält jeweils Sommer- und Wintersemester für das Vorjahr, das aktuelle Jahr und das Folgejahr.
     *
     * @return Liste der Semesterbezeichnungen im Format "Semesterart Jahr"
     */
    public List<String> getMomSemesters() {
        List<String> momSemesters = new ArrayList<>();

        List<String> veranstaltungsSemester = new ArrayList<>();

        OptionalInt firstYear;

        veranstaltungService.findAllVeranstaltungen("").forEach(veranstaltung -> veranstaltungsSemester.add(veranstaltung.getSemester()));
        firstYear = veranstaltungsSemester.stream()
                .distinct()
                .mapToInt(semester ->
                        Integer.parseInt(semester.strip()
                                .substring(semester.indexOf(" ")).strip()))
                .min();

        if (firstYear.isPresent()) {
            int year = firstYear.getAsInt();
            int now = LocalDate.now().getYear();

            for (int i = now+1; i >= year; i--) {
                momSemesters.add(new Semester(SemesterEnum.Wintersemester, i).toString());
                momSemesters.add(new Semester(SemesterEnum.Sommersemester, i).toString());
            }
        } else {
            int previousYear = LocalDate.now().getYear() - 1;
            int currentYear = LocalDate.now().getYear();
            int nextYear = LocalDate.now().getYear() + 1;

            momSemesters.add(new Semester(SemesterEnum.Wintersemester, nextYear).toString());
            momSemesters.add(new Semester(SemesterEnum.Sommersemester, nextYear).toString());

            momSemesters.add(new Semester(SemesterEnum.Wintersemester, currentYear).toString());
            momSemesters.add(new Semester(SemesterEnum.Sommersemester, currentYear).toString());

            momSemesters.add(new Semester(SemesterEnum.Wintersemester, previousYear).toString());
            momSemesters.add(new Semester(SemesterEnum.Sommersemester, previousYear).toString());
        }

        return momSemesters;
    }
}