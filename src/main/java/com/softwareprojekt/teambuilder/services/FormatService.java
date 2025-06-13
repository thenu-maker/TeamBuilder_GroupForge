package com.softwareprojekt.teambuilder.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//Author: Silas Weber
public class FormatService {

    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
