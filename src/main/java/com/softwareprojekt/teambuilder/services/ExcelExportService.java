package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.util.List;
//Author: Fiona Sander
@Service
public class ExcelExportService {

    /**
     * Exportiert eine Teilnehmerübersicht als Excel-Datei.
     *
     * @param teilnehmerListe Liste der Teilnehmer
     * @return InputStream für die Excel-Datei
     * @throws Exception bei Fehlern im Exportprozess
     */
    public ByteArrayInputStream exportTeilnahmeAuswertung(List<Teilnehmer> teilnehmerListe) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Teilnehmerübersicht");
            CreationHelper createHelper = workbook.getCreationHelper();

            String[] headers = {
                    "Matrikelnummer", "Vorname", "Nachname",
                    "Termin", "Startzeit", "Endzeit",
                    "Gruppenarbeit", "Gruppe", "Gruppenanmerkung",
                    "Leistungspunkte", "Präsentationspunkte"
            };

            // Header-Style (schwarzer Text auf weißem Hintergrund)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Datums- und Zeit-Styles
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));

            CellStyle timeStyle = workbook.createCellStyle();
            timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));

            CellStyle defaultStyle = workbook.createCellStyle();

            // Zebra-Stile
            CellStyle zebraDateStyle = cloneWithZebra(workbook, dateStyle);
            CellStyle zebraTimeStyle = cloneWithZebra(workbook, timeStyle);
            CellStyle zebraDefaultStyle = cloneWithZebra(workbook, defaultStyle);

            // Header-Zeile
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Teilnehmer teilnehmer : teilnehmerListe) {
                for (Teilnahme teilnahme : teilnehmer.getTeilnahmen()) {
                    if (teilnahme.getGruppe() == null ||
                            teilnahme.getGruppe().getGruppenarbeit() == null ||
                            teilnahme.getGruppe().getGruppenarbeit().getTermin() == null)
                        continue;

                    Gruppe gruppe = teilnahme.getGruppe();
                    Gruppenarbeit arbeit = gruppe.getGruppenarbeit();
                    Termin termin = arbeit.getTermin();

                    boolean zebra = (rowIdx % 2 != 0);
                    Row row = sheet.createRow(rowIdx++);
                    int col = 0;

                    CellStyle textStyle = zebra ? zebraDefaultStyle : defaultStyle;
                    CellStyle dateCellStyle = zebra ? zebraDateStyle : dateStyle;
                    CellStyle timeCellStyle = zebra ? zebraTimeStyle : timeStyle;

                    Cell matrnrCell = row.createCell(col++);
                    matrnrCell.setCellValue(teilnehmer.getMatrnr());
                    matrnrCell.setCellStyle(textStyle);

                    Cell vornameCell = row.createCell(col++);
                    vornameCell.setCellValue(teilnehmer.getVorname());
                    vornameCell.setCellStyle(textStyle);

                    Cell nachnameCell = row.createCell(col++);
                    nachnameCell.setCellValue(teilnehmer.getNachname());
                    nachnameCell.setCellStyle(textStyle);

                    Cell dateCell = row.createCell(col++);
                    dateCell.setCellValue(java.sql.Date.valueOf(termin.getDatum()));
                    dateCell.setCellStyle(dateCellStyle);

                    Cell startCell = row.createCell(col++);
                    startCell.setCellValue(Time.valueOf(termin.getStartzeit()));
                    startCell.setCellStyle(timeCellStyle);

                    Cell endCell = row.createCell(col++);
                    endCell.setCellValue(Time.valueOf(termin.getEndzeit()));
                    endCell.setCellStyle(timeCellStyle);

                    Cell arbeitCell = row.createCell(col++);
                    arbeitCell.setCellValue(arbeit.getTitel());
                    arbeitCell.setCellStyle(textStyle);

                    Cell gruppeCell = row.createCell(col++);
                    gruppeCell.setCellValue(gruppe.getGruppenname());
                    gruppeCell.setCellStyle(textStyle);

                    Cell gruppeAnmerkungCell = row.createCell(col++);
                    gruppeAnmerkungCell.setCellValue(gruppe.getAnmerkung());
                    gruppeAnmerkungCell.setCellStyle(textStyle);

                    Cell lp = row.createCell(col++);
                    lp.setCellValue(teilnahme.getLeistungspunkte());
                    lp.setCellStyle(textStyle);

                    Cell pp = row.createCell(col++);
                    pp.setCellValue(teilnahme.getPraesentationspunkte());
                    pp.setCellStyle(textStyle);
                }
            }

            // Feste Spaltenbreiten setzen
            int[] columnWidths = {
                    18, // Matrikelnummer
                    15, // Vorname
                    20, // Nachname
                    12, // Termin (Datum)
                    10, // Startzeit
                    10, // Endzeit
                    25, // Gruppenarbeit
                    20, // Gruppe
                    30, // Gruppenanmerkung
                    20, // Leistungspunkte
                    20  // Präsentationspunkte
            };
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256); // 1 Einheit = 1/256 Zeichenbreite
            }

            // Filter setzen
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, headers.length - 1));


            // Neues Sheet
            Sheet summarySheet = workbook.createSheet("Teilnehmerauswertung");

            // Header
            String[] summaryHeaders = {
                    "Matrikelnummer", "Vorname", "Nachname",
                    "Anzahl Teilnahmen", "Anzahl Gruppenarbeiten",
                    "Ø Leistungspunkte", "Ø Präsentationspunkte"
            };

            Row summaryHeader = summarySheet.createRow(0);
            for (int i = 0; i < summaryHeaders.length; i++) {
                Cell cell = summaryHeader.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int summaryRowIdx = 1;
            for (Teilnehmer t : teilnehmerListe) {
                List<Teilnahme> alleteilnahmen = t.getTeilnahmen();
                List<Teilnahme> teilnahmen = alleteilnahmen.stream()
                        .filter(teilnahme -> teilnahme.getGruppe() != null
                                && teilnahme.getGruppe().getGruppenarbeit() != null
                                && teilnahme.getGruppe().getGruppenarbeit().getTermin() != null)
                        .toList();

                long teilnahmenCount = teilnahmen.size();
                long gruppenarbeitenCount = teilnahmen.stream()
                        .map(teilnahme -> teilnahme.getGruppe().getGruppenarbeit().getId())
                        .distinct()
                        .count();

                double avgLP = teilnahmen.stream()
                        .mapToDouble(Teilnahme::getLeistungspunkte)
                        .average()
                        .orElse(0.0);

                double avgPP = teilnahmen.stream()
                        .mapToDouble(Teilnahme::getPraesentationspunkte)
                        .average()
                        .orElse(0.0);

                Row row = summarySheet.createRow(summaryRowIdx++);
                int col = 0;

                CellStyle rowStyle = (summaryRowIdx % 2 == 0) ? zebraDefaultStyle : defaultStyle;

                Cell matrnrCell = row.createCell(col++);
                matrnrCell.setCellValue(t.getMatrnr());
                matrnrCell.setCellStyle(rowStyle);

                Cell vornameCell = row.createCell(col++);
                vornameCell.setCellValue(t.getVorname());
                vornameCell.setCellStyle(rowStyle);

                Cell nachnameCell = row.createCell(col++);
                nachnameCell.setCellValue(t.getNachname());
                nachnameCell.setCellStyle(rowStyle);

                Cell countCell = row.createCell(col++);
                countCell.setCellValue(teilnahmenCount);
                countCell.setCellStyle(rowStyle);

                Cell gruppenCell = row.createCell(col++);
                gruppenCell.setCellValue(gruppenarbeitenCount);
                gruppenCell.setCellStyle(rowStyle);

                Cell avgLPCell = row.createCell(col++);
                avgLPCell.setCellValue(avgLP);
                avgLPCell.setCellStyle(rowStyle);

                Cell avgPPCell = row.createCell(col++);
                avgPPCell.setCellValue(avgPP);
                avgPPCell.setCellStyle(rowStyle);
            }

            // Feste Spaltenbreiten
            int[] summaryColumnWidths = {
                18, // Matrikelnummer
                15, // Vorname
                20, // Nachname
                25, // Anzahl Teilnahmen
                25, // Anzahl Gruppenarbeiten
                23, // Durchschnitt Leistungspunkte
                23  // Durchschnitt Präsentationspunkte
            };
            for (int i = 0; i < summaryHeaders.length; i++) {
                summarySheet.setColumnWidth(i, summaryColumnWidths[i] * 256);
            }



            summarySheet.setAutoFilter(new CellRangeAddress(0, summarySheet.getLastRowNum(), 0, summaryHeaders.length - 1));


            // Output stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }


    private CellStyle cloneWithZebra(XSSFWorkbook wb, CellStyle base) {
        CellStyle copy = wb.createCellStyle();
        copy.cloneStyleFrom(base);
        copy.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        copy.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return copy;
    }
}
