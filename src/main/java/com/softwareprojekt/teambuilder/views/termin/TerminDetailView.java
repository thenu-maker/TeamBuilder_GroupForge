package com.softwareprojekt.teambuilder.views.termin;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.services.*;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.GruppenarbeitBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerHinzufuegenDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//Author: Thenujan Karunakumar
@Route(value = "/termin/:terminid", layout = MainLayout.class)
@PageTitle("Termin | GroupForge")
@PermitAll
public class TerminDetailView extends HorizontalLayout implements BeforeEnterObserver{

    private final GruppenarbeitService gruppenarbeitService;
    private final TerminService terminService;
    private final TeilnahmeService teilnahmeService;
    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;
    private final HorizontalLayout removeButtonContainer = new HorizontalLayout();
    private final TextField titelField = new TextField();
    private final Button addButton = new Button("+");
    private final Grid<Gruppenarbeit> gruppenarbeitGrid = new Grid<>(Gruppenarbeit.class, false);
    private final Grid<Teilnehmer> teilnehmerGrid = new Grid<>(Teilnehmer.class, false);
    private Termin aktuellerTermin;
    private Veranstaltung akutelleVeranstaltung;
    private final H1 header = new H1("Gruppenarbeiten"); // Initialwert

    private final DatePicker datumPicker = new DatePicker("Datum");
    private final TimePicker startzeitPicker = new TimePicker("Startzeit");
    private final TimePicker endzeitPicker = new TimePicker("Endzeit");

    private final Span speicherStatus = new Span();

    private final Button abbrechenButton = new Button("Abbrechen");
    private LocalDate ursprünglichesDatum;
    private LocalTime ursprünglicheStartzeit;
    private LocalTime ursprünglicheEndzeit;
    private final Button removeButton = new Button("Termin löschen", VaadinIcon.TRASH.create());

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final VerticalLayout terminLayout = new VerticalLayout();
    private List<Teilnehmer> teilnehmerList = new ArrayList<>();;


    private final Button zurueckButton = new Button("Zurück zur Übersicht");



    @Autowired
    public TerminDetailView(GruppenarbeitService gruppenarbeitService,
                            TerminService terminService,
                            TeilnahmeService teilnahmeService,
                            VeranstaltungService veranstaltungService,
                            TeilnehmerService teilnehmerService) {
        this.gruppenarbeitService = gruppenarbeitService;
        this.teilnehmerService = teilnehmerService;
        this.terminService = terminService;
        this.teilnahmeService = teilnahmeService;
        this.veranstaltungService = veranstaltungService;

        mainLayout.add(header,zurueckButton);

        H1 daten = new H1("Termin-Daten:");
        daten.addClassNames("text-xl");

        startzeitPicker.setStep(Duration.ofMinutes(15));
        endzeitPicker.setStep(Duration.ofMinutes(15));
        datumPicker.setEnabled(false);
        startzeitPicker.setEnabled(false);
        endzeitPicker.setEnabled(false);


        removeButton.getElement().getThemeList().add("error");
        removeButton.addClickListener(e -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Termin löschen?");
            confirmDialog.setText("Möchten Sie diesen Termin wirklich löschen? Alle zugehörigen Daten (Gruppenarbeiten, Teilnahmen) werden ebenfalls gelöscht.");
            confirmDialog.setCancelable(true);
            confirmDialog.setCancelText("Abbrechen");
            confirmDialog.setConfirmText("Löschen");
            confirmDialog.setConfirmButtonTheme("error primary");

            confirmDialog.addConfirmListener(event -> {
                try {
                    Termin terminZumLoeschen = terminService.findTerminById(aktuellerTermin.getId());
                    terminService.deleteTerminMitAbhaengigkeiten(terminZumLoeschen);

                    Notification.show("Termin erfolgreich gelöscht", 3000, Notification.Position.MIDDLE);


                    if (akutelleVeranstaltung != null) {
                        UI.getCurrent().navigate("veranstaltung/" + akutelleVeranstaltung.getId());
                    } else {
                        UI.getCurrent().navigate("veranstaltungen");
                    }
                } catch (Exception ex) {
                    Notification.show("Fehler beim Löschen des Termins: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE);
                    ex.printStackTrace();
                }
            });

            confirmDialog.open();
        });




        removeButtonContainer.add(removeButton);
        removeButtonContainer.setWidthFull();
        removeButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        removeButtonContainer.setVisible(false); // Anfangs unsichtbar

        Button bearbeitenButton = new Button("Bearbeiten");
        bearbeitenButton.addClickListener(e -> {
            boolean isLocked = !datumPicker.isEnabled();
            if (isLocked) {
                // Werte puffern
                ursprünglichesDatum = datumPicker.getValue();
                ursprünglicheStartzeit = startzeitPicker.getValue();
                ursprünglicheEndzeit = endzeitPicker.getValue();

                datumPicker.setEnabled(true);
                startzeitPicker.setEnabled(true);
                endzeitPicker.setEnabled(true);
                abbrechenButton.setVisible(true);
                removeButtonContainer.setVisible(true);
            }


            if (!isLocked){
                if (saveTermin()){
                    datumPicker.setEnabled(false);
                    startzeitPicker.setEnabled(false);
                    endzeitPicker.setEnabled(false);
                    abbrechenButton.setVisible(false);
                    removeButtonContainer.setVisible(false);
                }
            }

            bearbeitenButton.setText(isLocked ? "Speichern" : "Bearbeiten");
        });

        abbrechenButton.setVisible(false);
        abbrechenButton.addClickShortcut(Key.ESCAPE);
        abbrechenButton.addClickListener(e -> {

            datumPicker.setValue(ursprünglichesDatum);
            startzeitPicker.setValue(ursprünglicheStartzeit);
            endzeitPicker.setValue(ursprünglicheEndzeit);


            datumPicker.setEnabled(false);
            startzeitPicker.setEnabled(false);
            endzeitPicker.setEnabled(false);
            bearbeitenButton.setText("Bearbeiten");
            abbrechenButton.setVisible(false);
            removeButtonContainer.setVisible(false);
        });
        HorizontalLayout terminButtons = new HorizontalLayout(bearbeitenButton, abbrechenButton);

        terminLayout.add(daten, datumPicker, startzeitPicker, endzeitPicker,terminButtons, removeButtonContainer,speicherStatus);
        terminLayout.setWidth("20%");
        terminLayout.setHeightFull();
        terminLayout.addClassName("sticky-layout");


        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
        Component anwesenheitSheet = getAnwesenheitsSheet();
        Component gruppenarbeitSheet = getGruppenarbeitSheet();

        tabSheet.add("Anwesenheit", anwesenheitSheet);
        tabSheet.add("Gruppenarbeiten", gruppenarbeitSheet);



        mainLayout.setSizeFull();
        mainLayout.add(tabSheet, terminLayout);

        add(mainLayout);
        add(terminLayout);


    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var routeParams = event.getRouteParameters();

        String terminIdStr = routeParams.get("terminid").orElse(null);

        if (terminIdStr == null) {
            throw new IllegalArgumentException("Ungültige Termin-ID oder Termin nicht gefunden.");
        }

        try {

            Long terminId = Long.parseLong(terminIdStr);
            Termin termin = terminService.findTerminById(terminId);
            if (termin != null) {
                aktuellerTermin = termin;
                akutelleVeranstaltung= aktuellerTermin.getVeranstaltung();


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                String datum = aktuellerTermin.getDatum().format(formatter);

                setHeaderText(akutelleVeranstaltung.getTitel() + " – " + datum);

                datumPicker.setValue(aktuellerTermin.getDatum());
                startzeitPicker.setValue(aktuellerTermin.getStartzeit());
                endzeitPicker.setValue(aktuellerTermin.getEndzeit());

                gruppenarbeitGrid.setItems(gruppenarbeitService.findByTermin(aktuellerTermin));

                zurueckButton.addClickListener(e ->
                        UI.getCurrent().navigate("veranstaltung/" + akutelleVeranstaltung.getId())



                );

                teilnehmerList.addAll(Stream.concat(
                                akutelleVeranstaltung.getTeilnehmer().stream(),
                                aktuellerTermin.getTeilnehmer().stream()
                        )
                        .distinct()
                        .toList());

                populateGrid();


            } else {
                throw new IllegalArgumentException("Ungültige Termin-ID oder Termin nicht gefunden.");

            }



        } catch (NumberFormatException e) {
            add(new Span("Ungültige Termin-ID."));
        }
    }


    private void setHeaderText(String text) {
        header.setText(text);
    }

    private void populateGrid(){

        teilnehmerGrid.setItems(teilnehmerList);
        gruppenarbeitGrid.setItems(gruppenarbeitService.findByTermin(aktuellerTermin));
    }

    private void addGruppenarbeit() {
        String titel = titelField.getValue().trim();
        if (titel.isEmpty()) {
            Notification.show("Bitte einen Titel eingeben.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (aktuellerTermin != null) {
            Gruppenarbeit neueArbeit = new Gruppenarbeit(titel);
            neueArbeit.setTermin(aktuellerTermin);
            gruppenarbeitService.save(neueArbeit);
            gruppenarbeitGrid.setItems(gruppenarbeitService.findByTermin(aktuellerTermin));
            titelField.clear();
            Notification.show("Gruppenarbeit angelegt", 3000, Notification.Position.MIDDLE);
        }
    }

    private boolean saveTermin() {
        LocalDate datum = datumPicker.getValue();
        LocalTime startzeit = startzeitPicker.getValue();
        LocalTime endzeit = endzeitPicker.getValue();

        if (datum == null || startzeit == null || endzeit == null) {
            speicherStatus.setText("Bitte fülle alle Felder aus.");
            return false;
        }

        if (startzeit.isAfter(endzeit) || startzeit.equals(endzeit)) {
            speicherStatus.setText("Die Startzeit muss vor der Endzeit liegen.");
            return false;
        }


        aktuellerTermin.setDatum(datum);
        aktuellerTermin.setStartzeit(startzeit);
        aktuellerTermin.setEndzeit(endzeit);
        terminService.save(aktuellerTermin);
        speicherStatus.setText("Termin erfolgreich gespeichert.");
        return true;
    }


    private Component getGruppenarbeitSheet(){
        VerticalLayout sheet = new VerticalLayout();

        Span neuLabel = new Span("Neu:");
        titelField.setPlaceholder("Titel");
        addButton.addClickListener(e -> addGruppenarbeit());
        HorizontalLayout inputLayout = new HorizontalLayout(neuLabel, titelField, addButton);
        inputLayout.setVerticalComponentAlignment(Alignment.CENTER, neuLabel, titelField, addButton);
        sheet.add(inputLayout);

        addButton.addClickShortcut(Key.ENTER);

        gruppenarbeitGrid.addColumn(Gruppenarbeit::getTitel).setHeader("Titel");
        gruppenarbeitGrid.addItemDoubleClickListener(event -> {
            Gruppenarbeit aktuelleGruppenarbeit = event.getItem();
            if (aktuelleGruppenarbeit != null) {
                List<Teilnahme> gruppenExistieren = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
                if (gruppenExistieren.isEmpty()) {

                    UI.getCurrent().navigate("gruppenarbeit/" +aktuelleGruppenarbeit.getId());
                } else {
                    UI.getCurrent().navigate("gruppenarbeit/" +aktuelleGruppenarbeit.getId()+ "/anzeige");
                }
            }
        });

        gruppenarbeitGrid.setTooltipGenerator(g ->
                "Doppelklick um die Gruppenarbeit " + g.getTitel() + " zu öffnen \n \n" +
                "Rechtsklick zum Bearbeiten oder Löschen von " + g.getTitel()
        );

        gruppenarbeitGrid.getElement().getStyle().set("user-select", "none");

        GridContextMenu<Gruppenarbeit> contextMenu = new GridContextMenu<>(gruppenarbeitGrid);

        contextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Bearbeiten")),
                event -> {
                    event.getItem().ifPresent(gruppenarbeit -> {
                        GruppenarbeitBearbeitenDialog bearbeitenDialog = new GruppenarbeitBearbeitenDialog(gruppenarbeitService, gruppenarbeit);
                        bearbeitenDialog.setSaveCallback(updated -> populateGrid());
                        bearbeitenDialog.open();
                    });
                }
        );


        contextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Löschen")),
                e -> deleteGruppenarbeitEvent(e));



        sheet.add(gruppenarbeitGrid);

        return sheet;
    }

    private void deleteGruppenarbeitEvent(GridContextMenu.GridContextMenuItemClickEvent<Gruppenarbeit> event) {

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Gruppenarbeit löschen?");
        dialog.setText("Möchten Sie die Gruppenarbeit " + event.getItem().get().getTitel() +" wirklich löschen?");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            event.getItem().ifPresent(gruppenarbeit -> {
                gruppenarbeitService.deleteGruppenarbeitMitAbhaengigkeiten(gruppenarbeit); // Aus der Liste entfernen
                populateGrid();// Grid aktualisieren
                Notification.show("Gruppenarbeit entfernt.", 3000, Notification.Position.MIDDLE);
            });
        });

        dialog.open();
    }

    private Component getAnwesenheitsSheet() {
        VerticalLayout sheet = new VerticalLayout();

        Button addTeilnhemerButton = new Button("Hinzufügen", VaadinIcon.PLUS.create());
        addTeilnhemerButton.addClickListener(e -> {
            TeilnehmerHinzufuegenDialog dialog = new TeilnehmerHinzufuegenDialog(teilnehmerService);

            // Callback setzen
            dialog.setSaveCallback(teilnehmer -> {
                if (teilnehmerList.stream().anyMatch(t -> t.getMatrnr() == teilnehmer.getMatrnr())) {
                    Notification.show("Teilnehmer bereits vorhanden!", 3000, Notification.Position.MIDDLE);
                    return;
                }
                teilnehmerList.add(teilnehmer);
                aktuellerTermin.addTeilnehmer(teilnehmer);

                terminService.save(aktuellerTermin);
                teilnehmerGrid.setItems(teilnehmerList);
            });
            dialog.open();
        });

        sheet.add(addTeilnhemerButton);

        teilnehmerGrid.addColumn(Teilnehmer::getMatrnr).setHeader("Matrikelnummer").setSortable(true);
        teilnehmerGrid.addColumn(Teilnehmer::getVorname).setHeader("Vorname").setSortable(true);
        teilnehmerGrid.addColumn(Teilnehmer::getNachname).setHeader("Nachname").setSortable(true);

        teilnehmerGrid.addColumn(new ComponentRenderer<>(teilnehmer -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(aktuellerTermin.hatTeilnehmer(teilnehmer));

            checkbox.addValueChangeListener(e -> {
                checkbox.setEnabled(false); // verhindert Doppelklicks
                aktuellerTermin= terminService.findTerminById(aktuellerTermin.getId());


                try {
                    boolean istSchonHinzugefügt = aktuellerTermin.hatTeilnehmer(teilnehmer);

                    if (e.getValue()) {
                        if (!istSchonHinzugefügt) {
                            aktuellerTermin.addTeilnehmer(teilnehmer);
                            terminService.save(aktuellerTermin);
                        }
                    } else {
                        if (istSchonHinzugefügt) {
                            aktuellerTermin.removeTeilnehmer(teilnehmer);
                            terminService.save(aktuellerTermin);
                        }
                    }
                    aktuellerTermin= terminService.findTerminById(aktuellerTermin.getId());


                } catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    ex.printStackTrace();
                } finally {
                    checkbox.setEnabled(true);
                }
            });

            return checkbox;
        })).setHeader("Anwesend").setSortable(true);

        teilnehmerGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        sheet.add(teilnehmerGrid);

        configureGridActions();

        return sheet;
    }

    private void configureGridActions() {

        teilnehmerGrid.addItemDoubleClickListener(event -> {
            Teilnehmer teilnehmer = event.getItem();


            TeilnehmerBearbeitenDialog bearbeitenDialog = new TeilnehmerBearbeitenDialog(teilnehmerService, teilnehmer);


            bearbeitenDialog.setSaveCallback(updatedTeilnehmer -> {

                teilnehmerGrid.getDataProvider().refreshAll();
            });

            bearbeitenDialog.open();
        });


        teilnehmerGrid.setTooltipGenerator(t ->
                "Doppelklick zum Bearbeiten des Teilnehmers " + t.getVorname() + " " + t.getNachname()
        );

        teilnehmerGrid.getElement().getStyle().set("user-select", "none");
    }
}




