package com.softwareprojekt.teambuilder.entities;

import com.softwareprojekt.teambuilder.services.SemesterService;

//Author: Silas Weber
public class Semester {

    private final SemesterService.SemesterEnum semester;
    private final int jahr;

    public Semester(SemesterService.SemesterEnum semester, int jahr) {
        this.semester = semester;
        this.jahr = jahr;
    }

    @Override
    public String toString() {
        return semester.name() + " " + jahr;
    }
}
