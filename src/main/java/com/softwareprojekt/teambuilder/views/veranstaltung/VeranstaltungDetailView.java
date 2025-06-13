package com.softwareprojekt.teambuilder.views.veranstaltung;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.services.*;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerHinzufuegenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TermineBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TermineHinzufuegenDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//Author: Tolga Cenk Kilic
@Route(value = "veranstaltung/:Id", layout = MainLayout.class)
@PageTitle("Veranstaltungsdetails | GroupForge")
@PermitAll
public class VeranstaltungDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final VeranstaltungService veranstaltungService;
    private long id;
    private final TerminService terminService;
    private final TeilnehmerService teilnehmerService;
    private String titel;
    private final List<Teilnehmer>teilnehmerList = new ArrayList<Teilnehmer>();
    private List<Termin> terminList = new ArrayList<>();
    private Grid<Teilnehmer> teilnehmerGrid;
    Button teilnehmerHinzufuegenButton = new Button("Teilnehmer hinzufügen",VaadinIcon.PLUS.create());
    Button terminHinzufuegenButton = new Button("Termin hinzufügen",VaadinIcon.PLUS.create());
    private final ExcelExportService excelExportService;
    private Veranstaltung veranstaltung = null;
    private Grid<Termin> terminGrid;
    private Button loeschenButton = new Button("Veranstaltung Löschen", VaadinIcon.TRASH.create());

    // Konstruktor
    public VeranstaltungDetailView(VeranstaltungService veranstaltungService,
                                   TerminService terminService,
                                   TeilnehmerService teilnehmerService,
                                   ExcelExportService excelExportService) {
        this.veranstaltungService = veranstaltungService;
        this.terminService = terminService;
        this.teilnehmerService = teilnehmerService;
        this.excelExportService = excelExportService;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Button zurueckButton = new Button("Zurück zur Übersicht");
        zurueckButton.addClickListener(e -> UI.getCurrent().navigate("veranstaltungen"));
        loeschenButton.addClickListener(e -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Veranstaltung löschen");
            confirmDialog.setText("Möchten Sie diese Veranstaltung wirklich löschen?");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmButton("Löschen", e1 -> {
                veranstaltungService.deleteVeranstaltungMitAbhaengigkeiten(veranstaltung);
                Notification.show("Veranstaltung gelöscht", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("veranstaltungen");
            });
            confirmDialog.setCancelButton("Abbrechen", e1 -> confirmDialog.close());
            confirmDialog.open();
        });

        Button auswertungButton = new Button("Auswertung herunterladen");


        String SId= event.getRouteParameters().get("Id").orElse(null);
        if(SId== null)
        {
            throw new NullPointerException("Veranstaltungs-ID muss angegeben sein");
        }
        else{
            try{
                this.id = Long.parseLong(SId);
                veranstaltung = veranstaltungService.findVeranstaltungById(id);

            }
            catch (NumberFormatException e){
                throw new NumberFormatException("Veranstaltungs-ID muss eine Zahl sein");
            }
        }

        if (veranstaltung != null) {

            this.teilnehmerList.clear();
            this.terminList.clear();
            this.teilnehmerList.addAll(veranstaltung.getTeilnehmer());
            this.terminList.addAll(terminService.findAllTermineByVeranstaltung(veranstaltung));

            if (terminGrid != null) {
                terminGrid.setItems(this.terminList);
            }

            H1 title = new H1(veranstaltung.getTitel());
            Span semesterSpan = new Span("Semester: " + veranstaltung.getSemester());


            add(title);
            add(semesterSpan);
            add(zurueckButton);
            String veranstaltungsTitel = veranstaltung.getTitel().replaceAll("\\s+", "_");
            String heutigesDatum = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
            String dateiname = "Auswertung_" + veranstaltungsTitel + "_" + heutigesDatum + ".xlsx";
            StreamResource resource = new StreamResource(dateiname, () -> {
                try {
                    List<Termin> terminListe= terminService.findAllTermineByVeranstaltung(veranstaltung);
                    List<Teilnehmer> teilnehmerListe = teilnehmerService.findAllTeilnehmerByTermine(terminListe);
                    return excelExportService.exportTeilnahmeAuswertung(teilnehmerListe);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });

            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.add(auswertungButton);

            HorizontalLayout buttonLayout = new HorizontalLayout(zurueckButton, downloadLink);
            add(buttonLayout);

            TabSheet tabSheet = new TabSheet();
            tabSheet.setWidthFull();
            Tab terminTab = new Tab("Termine");
            VerticalLayout terminLayout = createTerminTab(veranstaltung);

            Tab teilnehmerTab = new Tab("Teilnehmer");
            VerticalLayout teilnehmerLayout = createTeilnehmerTab(veranstaltung);
            tabSheet.add(teilnehmerTab, teilnehmerLayout);
            tabSheet.add(terminTab, terminLayout);

            add(tabSheet);

            terminGrid.setItems(terminService.findAllTermineByVeranstaltung(veranstaltung));
        } else {
            throw new NullPointerException("Veranstaltung mit der ID " + id + " konnte nicht gefunden werden");
        }
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        add(loeschenButton);
    }

    private VerticalLayout createTeilnehmerTab(Veranstaltung veranstaltung) {
        VerticalLayout teilnehmerLayout = new VerticalLayout();
        teilnehmerGrid = new Grid<>(Teilnehmer.class);
        teilnehmerGrid.removeAllColumns();

        teilnehmerGrid.addColumn(Teilnehmer::getMatrnr)
                .setHeader("Matrikelnummer")
                .setSortable(true)
                .setFlexGrow(2);

        teilnehmerGrid.addColumn(Teilnehmer::getVorname)
                .setHeader("Vorname")
                .setSortable(true)
                .setFlexGrow(2);

        teilnehmerGrid.addColumn(Teilnehmer::getNachname)
                .setHeader("Nachname")
                .setSortable(true)
                .setFlexGrow(2);



        refreshTeilnehmerListe(veranstaltung);
        teilnehmerHinzufuegenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        teilnehmerHinzufuegenButton.addClickListener(e -> {
            TeilnehmerHinzufuegenDialog dialog = new TeilnehmerHinzufuegenDialog(teilnehmerService);

            dialog.setSaveCallback(teilnehmer -> {
                Veranstaltung aktuelleVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());

                boolean exists = aktuelleVeranstaltung.getTeilnehmer().stream()
                        .anyMatch(t -> t.getMatrnr() == teilnehmer.getMatrnr());

                if (exists) {
                    Notification.show("Teilnehmer mit Matrikelnummer " + teilnehmer.getMatrnr() + " ist bereits in der Liste", 3000, Notification.Position.MIDDLE);
                    return;
                }

                teilnehmerService.save(teilnehmer);
                aktuelleVeranstaltung.addTeilnehmer(teilnehmer);
                veranstaltungService.save(aktuelleVeranstaltung);

                refreshTeilnehmerListe(aktuelleVeranstaltung);
                Notification.show("Neuer Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde hinzugefügt", 3000, Notification.Position.MIDDLE);
            });

            dialog.open();
        });

        GridContextMenu<Teilnehmer> teilnehmerGridContextMenu = new GridContextMenu<>(teilnehmerGrid);
        teilnehmerGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Teilnehmer Bearbeiten")),
                event -> event.getItem().ifPresent(teilnehmer -> {
                    TeilnehmerBearbeitenDialog bearbeitenDialog = new TeilnehmerBearbeitenDialog(teilnehmerService, teilnehmer);
                    bearbeitenDialog.setSaveCallback(geänderterTeilnehmer -> refreshTeilnehmerListe(veranstaltung));
                    Notification.show("Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde aktualisiert", 3000, Notification.Position.MIDDLE);
                    bearbeitenDialog.open();
                })
        );

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
                    dialog.addConfirmListener(e -> event.getItem().ifPresent(teilnehmer -> {
                        try {

                            Veranstaltung aktuelleVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());


                            aktuelleVeranstaltung.removeTeilnehmer(teilnehmer);
                            veranstaltungService.save(aktuelleVeranstaltung);

                            Veranstaltung neuGeladeneVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());
                            refreshTeilnehmerListe(neuGeladeneVeranstaltung);

                            Notification.show("Teilnehmer mit der Matrikelnumnmer " + teilnehmer.getMatrnr() + " wurde aus der Liste entfernt",
                                    3000, Notification.Position.MIDDLE);
                        } catch (Exception ex) {
                            Notification.show("Fehler beim Entfernen: " + ex.getMessage(),
                                    3000, Notification.Position.MIDDLE);
                        }
                    }));
                    dialog.open();
                }
        );

        teilnehmerGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Teilnehmer Löschen")),
                event -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Teilnehmer Löschen?");
                    dialog.setText("Möchten Sie den Teilnehmer wirklich löschen? Dadurch wird der Teilnehmer aus der Datenbank entfernt und kann nicht wiederhergestellt werden.");
                    dialog.setCancelable(true);
                    dialog.setCancelText("Abbrechen");
                    dialog.setConfirmText("Löschen");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(e -> event.getItem().ifPresent(teilnehmer -> {
                        try {
                            Veranstaltung aktuelleVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());

                            // Teilnehmer entfernen
                            aktuelleVeranstaltung.removeTeilnehmer(teilnehmer);
                            veranstaltungService.save(aktuelleVeranstaltung);

                            // Teilnehmer aus DB löschen
                            teilnehmerService.deleteTeilnehmerMitAbhaengigkeiten(teilnehmer);

                            // Teilnehmerliste aktualisieren – komplette Veranstaltung neu laden
                            Veranstaltung neuGeladeneVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());
                            refreshTeilnehmerListe(neuGeladeneVeranstaltung);

                            Notification.show("Teilnehmer mit der Matrikelnummer " + teilnehmer.getMatrnr() + " wurde gelöscht", 3000, Notification.Position.MIDDLE);
                        } catch (Exception ex) {
                            Notification.show("Fehler beim Löschen: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                        }
                    }));
                    dialog.open();
                }
        );


        teilnehmerGrid.setTooltipGenerator(t ->
                "Rechtsklick zum Verwalten des Teilnehmers " + t.getVorname() + " " + t.getNachname()
        );

        teilnehmerLayout.add(new H2("Teilnehmer"));
        teilnehmerLayout.add(teilnehmerHinzufuegenButton, teilnehmerGrid);
        return teilnehmerLayout;
    }

    private VerticalLayout createTerminTab(Veranstaltung veranstaltung) {
        VerticalLayout terminLayout = new VerticalLayout();
        terminGrid = new Grid<>(Termin.class);
        terminGrid.removeAllColumns();

        terminGrid.addColumn(t -> FormatService.formatDate(t.getDatum()))
                .setHeader("Datum")
                .setSortable(true)
                .setFlexGrow(2);

        terminGrid.addColumn(Termin::getStartzeit)
                .setHeader("Startzeit")
                .setSortable(true)
                .setFlexGrow(1);

        terminGrid.addColumn(Termin::getEndzeit)
                .setHeader("Endzeit")
                .setSortable(true)
                .setFlexGrow(1);


        terminGrid.addItemDoubleClickListener(event -> {
            Termin termin = event.getItem();
            if (termin != null) {
                Long terminId = termin.getId();
                UI.getCurrent().navigate( "termin/" + terminId);
            }
        });

        terminHinzufuegenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        terminHinzufuegenButton.addClickListener(e -> {
            Veranstaltung aktuelleVeranstaltung = veranstaltungService.findVeranstaltungById(id);
            TermineHinzufuegenDialog dialog = new TermineHinzufuegenDialog(terminService, terminList);

            dialog.setSaveCallback(termin -> {
                // Beziehung zwischen Termin und Veranstaltung setzen
                termin.setVeranstaltung(aktuelleVeranstaltung);
                terminService.save(termin);

                // aktualisierte Termine laden
                List<Termin> aktualisierteTermine = terminService.findAllTermineByVeranstaltung(aktuelleVeranstaltung);
                terminGrid.setItems(aktualisierteTermine);
            });
            dialog.open();
        });

        GridContextMenu<Termin> terminGridContextMenu= new GridContextMenu<>(terminGrid);
        terminGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Termin Bearbeiten")),
                event -> event.getItem().ifPresent(termin -> {
                    TermineBearbeitenDialog bearbeitenDialog = new TermineBearbeitenDialog(terminService, termin);
                    bearbeitenDialog.setSaveCallback(geänderterTermin -> {
                        List<Termin> aktualisierteListe = terminService.findeAlleTermineByVeranstaltung(veranstaltung);
                        terminGrid.setItems(aktualisierteListe);
                        Notification.show("Termin wurde auf den " + FormatService.formatDate(termin.getDatum()) + " aktualisiert", 3000, Notification.Position.MIDDLE);
                    });
                    bearbeitenDialog.open();
                })
        );

        terminGridContextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Termin löschen")),
                event -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Termin löschen?");
                    dialog.setText("Möchten Sie den Termin wirklich löschen?");
                    dialog.setCancelable(true);
                    dialog.setCancelText("Abbrechen");
                    dialog.setConfirmText("Löschen");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(e -> event.getItem().ifPresent(termin -> {
                        try {
                            Termin aktuell = terminService.findTerminById(termin.getId());
                            if (aktuell == null) return;
                            terminService.deleteTerminMitAbhaengigkeiten(aktuell);
                            Veranstaltung v = veranstaltungService.findVeranstaltungById(id);
                            List<Termin> list = terminService.findAllTermineByVeranstaltung(v);
                            terminGrid.setItems(list);
                            Notification.show("Termin am " + FormatService.formatDate(termin.getDatum()) + " wurde gelöscht", 3000, Notification.Position.MIDDLE);
                        } catch (Exception ex) {
                            Notification.show("Fehler beim Löschen des Termins: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                        }
                    }));
                    dialog.open();
                }
        );


        // Tooltip pro Zeile anzeigen
        terminGrid.setTooltipGenerator(termin ->
                "Rechtsklick zum Verwalten des Termins am " + FormatService.formatDate(termin.getDatum())
        );

        terminLayout.add(new H2("Termine"));
        terminLayout.add(terminHinzufuegenButton,terminGrid);
        return terminLayout;
    }

    private void refreshTeilnehmerListe(Veranstaltung veranstaltung) {

        Veranstaltung aktuelleVeranstaltung = veranstaltungService.findVeranstaltungById(veranstaltung.getId());
        teilnehmerList.clear();
        teilnehmerList.addAll(aktuelleVeranstaltung.getTeilnehmer());
        teilnehmerGrid.setItems(teilnehmerList);
        teilnehmerGrid.getDataProvider().refreshAll();
    }
}
