package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.services.FormatService;
import com.softwareprojekt.teambuilder.services.TerminService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
//Author: Tolga Cenk Kilic
public class TermineHinzufuegenDialog extends AbstractTerminDialog {
    private final TerminService terminService;
    private Checkbox serienTerminCheckBox;
    private DatePicker serienEndDatum;
    private final List<Termin> lokaleTermine;

    public TermineHinzufuegenDialog(TerminService terminService, List<Termin> lokalTermine) {
        this.terminService = terminService;
        this.lokaleTermine = lokalTermine;
        layoutAusfuehren();
    }

    private void layoutAusfuehren() {
        setupCommonLayout("Termin hinzufügen");

        // Serientermin-Felder hinzufügen
        serienTerminCheckBox = new Checkbox("Serientermin erstellen?");
        serienEndDatum = new DatePicker("Serienende");
        serienEndDatum.setVisible(false);

        serienTerminCheckBox.addValueChangeListener(e -> {
            boolean isChecked = e.getValue();
            serienEndDatum.setVisible(isChecked);
            if (!isChecked) {
                serienEndDatum.clear();
            }
        });

        // Layout anpassen
        VerticalLayout fieldLayout = (VerticalLayout) getChildren()
                .filter(c -> c instanceof VerticalLayout)
                .findFirst().orElseThrow();
        fieldLayout.add(serienTerminCheckBox, serienEndDatum);

        saveButton.setText("Hinzufügen");
        saveButton.addClickListener(e -> speichern());
        cancelButton.addClickListener(e -> close());
    }

    @Override
    protected String getSaveButtonText() {
        return "Hinzufügen";
    }

    private boolean terminExistiert(Termin termin) {
        // Prüfe nur die lokalen Termine (ohne Veranstaltungsbezug)
        return lokaleTermine.stream()
                .anyMatch(t -> t.getDatum().equals(termin.getDatum())
                        && t.getStartzeit().equals(termin.getStartzeit())
                        && t.getEndzeit().equals(termin.getEndzeit()));
    }

    private List<Termin> createSeriesTermine(LocalDate initialDate, LocalTime start, LocalTime end) {
        List<Termin> termine = new ArrayList<>();
        LocalDate currentDate = initialDate;
        LocalDate endDate = serienEndDatum.getValue();

        List<Termin> kollidierendeTermine = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            Termin neuerTermin = new Termin(currentDate, start, end);

            if (terminExistiert(neuerTermin)) {
                kollidierendeTermine.add(neuerTermin);
            } else {
                termine.add(neuerTermin);
            }

            currentDate = currentDate.plusWeeks(1);
        }

        if (!kollidierendeTermine.isEmpty()) {
            showKollidierendeTermineWarning(kollidierendeTermine);
        }

        return termine;
    }

    private void showKollidierendeTermineWarning(List<Termin> kollidierendeTermine) {
        StringBuilder message = new StringBuilder("Folgende Termine existieren bereits und wurden übersprungen:\n");

        for (int i = 0; i < kollidierendeTermine.size(); i++) {
            Termin termin = kollidierendeTermine.get(i);
            message.append(FormatService.formatDate(termin.getDatum()))
                    .append(" von ")
                    .append(termin.getStartzeit())
                    .append(" bis ")
                    .append(termin.getEndzeit());

            if (i < kollidierendeTermine.size() - 1) {
                message.append(",\n");
            }
        }

        Notification.show(message.toString(), 5000, Notification.Position.MIDDLE);
    }

    private boolean validateSeriesDates(LocalDate initialDate) {
        if (serienEndDatum.getValue() == null) {
            Notification.show("Bitte ein Serien-Enddatum angeben");
            return false;
        }

        if (serienEndDatum.getValue().isBefore(initialDate)) {
            serienEndDatum.setErrorMessage("Das Datum für das Serienende muss nach dem Startdatum liegen");
            serienEndDatum.setInvalid(true);
            return false;
        }

        serienEndDatum.setInvalid(false);
        return true;
    }

    private void speichern() {
        if (!validateFields()) {
            return;
        }

        LocalDate date = datePicker.getValue();
        LocalTime start = startZeit.getValue();
        LocalTime end = endZeit.getValue();

        // Vorab-Prüfung für Einzeltermin
        if (!serienTerminCheckBox.getValue()) {
            Termin einzelTermin = new Termin(date, start, end);
            if (terminExistiert(einzelTermin)) {
                Notification.show("Ein Termin zu diesem Datum und Zeitraum existiert bereits",
                        3000, Notification.Position.MIDDLE);
                return;
            }
        }

        List<Termin> termine = new ArrayList<>();

        if (serienTerminCheckBox.getValue()) {
            if (!validateSeriesDates(date)) {
                return;
            }

            termine.addAll(createSeriesTermine(date, start, end));

            if (termine.isEmpty()) {
                Notification.show("Alle gewählten Serientermine existieren bereits",
                        3000, Notification.Position.MIDDLE);
                return;
            }
        } else {
            Termin einzelTermin = new Termin(date, start, end);
            termine.add(einzelTermin);
        }

        if (saveCallback != null) {
            termine.forEach(saveCallback);
        }

        if (serienTerminCheckBox.getValue()) {
            Notification.show(termine.size() + " Serientermine angelegt.", 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show("Einzeltermin angelegt.", 3000, Notification.Position.MIDDLE);
        }

        close();
    }
}