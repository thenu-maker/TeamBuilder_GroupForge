package com.softwareprojekt.teambuilder.views.veranstaltung;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.*;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerHinzufuegenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TermineBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TermineHinzufuegenDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Author: Tolga Cenk Kilic
@Route(value = "veranstaltungenErstellen", layout = MainLayout.class)
@PageTitle("Veranstaltungen Erstellen | GroupForge")
@PermitAll
public class VeranstaltungErstellenView extends VerticalLayout {

    private final TeilnehmerService teilnehmerService;
    private final TerminService terminService;
    private final VeranstaltungService veranstaltungService;
    private final BenutzerService benutzerService;
    private final SemesterService semesterService;

    private final Benutzer benutzer;

    private final List<Teilnehmer> teilnehmerList = new ArrayList<>();
    private final List<Termin> terminList = new ArrayList<>();

    private final TextField titelField = new TextField("Titel");
    private final ComboBox<String> semesterField = new ComboBox<>("Semester");
    private final Button addButton = new Button("Veranstaltung erstellen");
    private final Grid<Teilnehmer> gridTeilnehmer = new Grid<>(Teilnehmer.class);
    private final Grid<Termin> gridTermine = new Grid<>(Termin.class);

    public VeranstaltungErstellenView(TeilnehmerService teilnehmerService,
                                      TerminService terminService,
                                      VeranstaltungService veranstaltungService,
                                      SecurityService securityService,
                                      BenutzerService benutzerService,
                                      SemesterService semesterService) {

        this.teilnehmerService = teilnehmerService;
        this.terminService = terminService;
        this.veranstaltungService = veranstaltungService;
        this.benutzerService = benutzerService;
        this.semesterService = semesterService;
        this.benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());

        add(new H1("Veranstaltung erstellen"));


        HorizontalLayout inputLayout = new HorizontalLayout(titelField, semesterField);
        inputLayout.setVerticalComponentAlignment(Alignment.BASELINE, titelField, semesterField);
        add(inputLayout);


        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();


        gridTeilnehmer.setColumns("matrnr", "vorname", "nachname");
        Button teilnehmerHinzufuegenButton = new Button("Teilnehmer hinzufügen",VaadinIcon.PLUS.create());

        teilnehmerHinzufuegenButton.addClickListener(e -> {
            TeilnehmerHinzufuegenDialog dialog = new TeilnehmerHinzufuegenDialog(teilnehmerService);

            dialog.setSaveCallback(teilnehmer -> {

                boolean exists = false;
                for (Teilnehmer t : teilnehmerList) {
                    if (t.getMatrnr() == teilnehmer.getMatrnr()) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    Notification.show("Teilnehmer mit Matrikelnummer " + teilnehmer.getMatrnr() + " ist bereits in der Liste", 3000, Notification.Position.MIDDLE);
                    return;
                }

                teilnehmerList.add(teilnehmer);
                Notification.show("Neuer Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde hinzugefügt", 3000, Notification.Position.MIDDLE);
                gridTeilnehmer.setItems(teilnehmerList); // Grid aktualisieren
            });

            dialog.open();
        });

        GridContextMenu<Teilnehmer> teilnehmerGridContextMenu = new GridContextMenu<>(gridTeilnehmer);
        teilnehmerGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Teilnehmer bearbeiten")),
                event -> {
                    event.getItem().ifPresent(teilnehmer -> {
                        TeilnehmerBearbeitenDialog bearbeitenDialog = new TeilnehmerBearbeitenDialog(teilnehmerService, teilnehmer);

                        bearbeitenDialog.setSaveCallback(geänderterTeilnehmer -> {
                            gridTeilnehmer.setItems(teilnehmerList);
                            Notification.show("Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde aktualisiert", 3000, Notification.Position.MIDDLE);
                        });

                        bearbeitenDialog.open();
                    });
                });

        teilnehmerGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.MINUS_CIRCLE), new Span("Teilnehmer entfernen")),
                event -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Teilnehmer entfernen?");
                    dialog.setText("Möchten Sie den Teilnehmer wirklich entfernen?");

                    dialog.setCancelable(true);
                    dialog.setCancelText("Abbrechen");

                    dialog.setConfirmText("Entfernen");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(e -> {
                        event.getItem().ifPresent(teilnehmer -> {
                            boolean removed = teilnehmerList.remove(teilnehmer);
                            if (removed) {
                                gridTeilnehmer.setItems(teilnehmerList);
                                Notification.show("Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde entfernt", 3000, Notification.Position.MIDDLE);
                            }
                        });
                    });

                    dialog.open();
                });



        gridTeilnehmer.setTooltipGenerator(t ->
                "Rechtsklick zum Verwalten des Teilnehmers " + t.getVorname() + " " + t.getNachname()
        );



        VerticalLayout teilnehmerLayout = new VerticalLayout();
        HorizontalLayout buttonLayout = new HorizontalLayout(teilnehmerHinzufuegenButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.START);
        buttonLayout.setWidthFull();

        teilnehmerLayout.add(buttonLayout, gridTeilnehmer);
        tabSheet.add("Teilnehmer", teilnehmerLayout);

        semesterField.setItems(semesterService.getMomSemesters());
        teilnehmerHinzufuegenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        Button terminHinzufuegenButton = new Button("Termin hinzufügen",VaadinIcon.PLUS.create());
        terminHinzufuegenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        terminHinzufuegenButton.addClickListener(e -> {
            TermineHinzufuegenDialog dialog = new TermineHinzufuegenDialog(terminService, terminList);
            dialog.setSaveCallback(termin -> {

                Termin neuerTermin = new Termin();
                neuerTermin.setDatum(termin.getDatum());
                neuerTermin.setStartzeit(termin.getStartzeit());
                neuerTermin.setEndzeit(termin.getEndzeit());

                boolean exists = terminList.stream().anyMatch(t ->
                        t.getDatum().equals(neuerTermin.getDatum()) &&
                                t.getStartzeit().equals(neuerTermin.getStartzeit()) &&
                                t.getEndzeit().equals(neuerTermin.getEndzeit())
                );

                if (exists) {
                    Notification.show("Ein Termin mit diesen Zeiten existiert bereits.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                terminList.add(neuerTermin);
                gridTermine.setItems(terminList);
            });

            dialog.open();
        });

        GridContextMenu<Termin> terminGridContextMenu= new GridContextMenu<>(gridTermine);
        terminGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Termin bearbeiten")),
                event -> {
                    event.getItem().ifPresent(termin -> {
                        TermineBearbeitenDialog bearbeitenDialog = new TermineBearbeitenDialog(terminService, termin);

                        bearbeitenDialog.setSaveCallback(geänderterTermin -> {
                            gridTermine.setItems(terminList);
                            Notification.show("Termin wurde auf den " + FormatService.formatDate(termin.getDatum()) + " aktualisiert", 3000, Notification.Position.MIDDLE);
                        });

                        bearbeitenDialog.open();
                    });
                });

        terminGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Termin entfernen")),
                event -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Termin entfernen?");
                    dialog.setText("Möchten Sie den Termin wirklich entfernen?");

                    dialog.setCancelable(true);
                    dialog.setCancelText("Abbrechen");

                    dialog.setConfirmText("Entfernen");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(e -> {
                        event.getItem().ifPresent(termin -> {
                            boolean removed = terminList.remove(termin);
                            if (removed) {
                                gridTermine.setItems(terminList);
                                Notification.show("Termin am " + FormatService.formatDate(termin.getDatum()) + " wurde aus der Liste entfernt", 3000, Notification.Position.MIDDLE);
                            }
                        });
                    });

                    dialog.open();
                });

                    // Tooltip pro Zeile anzeigen
                    gridTermine.setTooltipGenerator(termin ->
                            "Rechtsklick zum Verwalten des Termins am " + FormatService.formatDate(termin.getDatum())
                    );

        VerticalLayout terminLayout = new VerticalLayout();
        gridTermine.removeAllColumns();

        gridTermine.addColumn(t -> FormatService.formatDate(t.getDatum()))
                .setHeader("Datum")
                .setSortable(true)
                .setFlexGrow(2);

        gridTermine.addColumn(Termin::getStartzeit)
                .setHeader("Startzeit")
                .setSortable(true)
                .setFlexGrow(1);

        gridTermine.addColumn(Termin::getEndzeit)
                .setHeader("Endzeit")
                .setSortable(true)
                .setFlexGrow(1);

                    HorizontalLayout terminButtonLayout = new HorizontalLayout(terminHinzufuegenButton);
                    terminButtonLayout.setJustifyContentMode(JustifyContentMode.START); // Button links ausrichten
                    terminButtonLayout.setWidthFull();

                    terminLayout.add(terminButtonLayout, gridTermine);
                    tabSheet.add("Termine", terminLayout); // TabSheet hinzufügen


                    // Tabsheet zur View hinzufügen
                    add(tabSheet);

        addButton.addClickListener(e ->{
        addVeranstaltung();});

        add(addButton);

    }

    public void addVeranstaltung() {
        String titel = titelField.getValue() != null ? titelField.getValue().trim() : "";
        String semesterText = semesterField.getValue() != null ? semesterField.getValue().toString().trim() : "";

        if (titel.isEmpty() && semesterText.isEmpty()) {
            Notification.show("Bitte Titel und Semester angeben.", 3000, Notification.Position.MIDDLE);
            return;
        } else if (titel.isEmpty()) {
            Notification.show("Bitte Titel angeben.", 3000, Notification.Position.MIDDLE);
            return;
        } else if (semesterText.isEmpty()) {
            Notification.show("Bitte Semester angeben.", 3000, Notification.Position.MIDDLE);
            return;
        }


        if (!titel.isEmpty() && !semesterText.isEmpty()) {
            try {

                Veranstaltung veranstaltung = veranstaltungService.findOrCreateVeranstaltung(titel, semesterText, benutzer.getId());

                Set<Teilnehmer> gespeicherteTeilnehmer = new HashSet<>();
                for (Teilnehmer teilnehmer : teilnehmerList) {

                    Teilnehmer gespeicherterTeilnehmer = teilnehmerService.findTeilnehmer(teilnehmer.getMatrnr());

                    if (gespeicherterTeilnehmer == null) {
                        teilnehmerService.save(teilnehmer);
                        gespeicherterTeilnehmer = teilnehmer;
                    }
                    gespeicherteTeilnehmer.add(gespeicherterTeilnehmer);
                }

                veranstaltung.setTeilnehmer(gespeicherteTeilnehmer);

                List<Termin> gespeicherteTermine = new ArrayList<>();
                for (Termin termin : terminList) {
                    termin.setVeranstaltung(veranstaltung);
                    terminService.save(termin);
                    gespeicherteTermine.add(termin);
                }
                veranstaltung.setTermine(gespeicherteTermine);

                veranstaltungService.save(veranstaltung);

                titelField.clear();
                semesterField.clear();
                Notification.show("Veranstaltung erfolgreich erstellt.", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("veranstaltungen");
            } catch (NumberFormatException e) {
                semesterField.setInvalid(true);
                semesterField.setErrorMessage("Bitte eine gültige Zahl eingeben.");
            }
        }
    }

    }

