package com.softwareprojekt.teambuilder.views.uebersicht;

import com.softwareprojekt.teambuilder.entities.*;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.*;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.GruppenarbeitBearbeitenDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerBearbeitenDialog;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

//Author: Silas Weber
@Route(value = "/gruppenarbeiten", layout = MainLayout.class)
@PageTitle("Gruppenarbeitenübersicht | GroupForge")
@PermitAll
public class GruppenarbeitenView extends VerticalLayout {

    private final TeilnahmeService teilnahmeService;
    private final SecurityService securityService;
    private final BenutzerService benutzerService;
    private final TeilnehmerService teilnehmerService;
    private final GruppeService gruppeService;
    private final SemesterService semesterService;
    private final GruppenarbeitService gruppenarbeitService;
    private Benutzer benutzer;


    Grid<Gruppenarbeit> gruppenarbeitenGrid = new Grid<>(Gruppenarbeit.class, false);
    TextField filterText = new TextField();
    ComboBox<String> semesterComboBox = new ComboBox<>();
    ComboBox<Veranstaltung> veranstaltungComboBox = new ComboBox<>();

    public GruppenarbeitenView(SecurityService securityService,
                               BenutzerService benutzerService,
                               TeilnehmerService teilnehmerService,
                               TeilnahmeService teilnahmeService,
                               GruppeService gruppeService,
                               SemesterService semesterService,
                               GruppenarbeitService gruppenarbeitService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;
        this.teilnehmerService = teilnehmerService;
        this.teilnahmeService = teilnahmeService;
        this.gruppeService = gruppeService;
        this.gruppenarbeitService = gruppenarbeitService;
        benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());


        this.semesterService = semesterService;

        setSizeFull();

        configureGrid();

        add(new H1("Gruppenarbeitenübersicht"));
        add(
                getToolbar(),
                gruppenarbeitenGrid
        );

        populateGrid();
    }

    private void configureGrid() {
        gruppenarbeitenGrid.setSizeFull();



                gruppenarbeitenGrid.addColumn(Gruppenarbeit::getTitel)
                        .setHeader("Titel")
                        .setSortable(true)
                        .setFlexGrow(3);

                gruppenarbeitenGrid.addColumn(g -> FormatService.formatDate(g.getTermin().getDatum()))
                        .setHeader("Datum")
                        .setSortable(true)
                        .setFlexGrow(1);

                gruppenarbeitenGrid.addColumn(g -> g.getTermin().getVeranstaltung().getTitel())
                        .setHeader("Veranstaltung")
                        .setSortable(true)
                        .setFlexGrow(2);

                gruppenarbeitenGrid.addColumn(g -> {
                            List<Gruppe> gruppen = gruppeService.findAllByGruppenarbeitId(g.getId());
                            List<Teilnahme> teilnhamen = new ArrayList<>();
                            gruppen.forEach(gruppe -> teilnhamen.addAll(gruppe.getTeilnahmen()));
                            return teilnhamen.size();
                        })
                        .setHeader("Teilnehmeranzahl")
                        .setSortable(true)
                        .setFlexGrow(1);

                gruppenarbeitenGrid.addColumn(
                                new ComponentRenderer<>(Button::new, (button, gruppenarbeit) -> {
                                    button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                                    button.addClickListener(e -> UI.getCurrent().navigate("gruppenarbeit/" + gruppenarbeit.getId() + "/bewerten"));
                                    button.setText("Bewerten");
                                }))
                        .setHeader("Bewerten")
                        .setFlexGrow(1);

        gruppenarbeitenGrid.addItemDoubleClickListener(event -> UI.getCurrent().navigate("gruppenarbeit/" + event.getItem().getId() + "/anzeige"));

        // Kontextmenü mit Icons
        GridContextMenu<Gruppenarbeit> contextMenu = new GridContextMenu<>(gruppenarbeitenGrid);

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

        // Löschen-Menüpunkt mit Icon
        contextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Löschen")),
                e -> deleteGruppenarbeitEvent(e));

        // Tooltip für Kontextmenü
        gruppenarbeitenGrid.setTooltipGenerator(g ->
                "Rechtsklick zum Bearbeiten oder Löschen von " + g.getTitel()
        );

}
    private void populateGrid() {
        List<Gruppenarbeit> gruppenarbeitList = new ArrayList<>();

        Benutzer benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());
        List<Veranstaltung> veranstaltungen = benutzer.getVeranstaltungen();

        if (veranstaltungComboBox.getValue() != null) {
            Veranstaltung selected = veranstaltungComboBox.getValue();
            veranstaltungen = veranstaltungen.stream()
                    .filter(v -> v.getId() == selected.getId())
                    .toList();
        }

        if (semesterComboBox.getValue() != null) {
            String selectedSemester = semesterComboBox.getValue().trim().toLowerCase();
            veranstaltungen = veranstaltungen.stream()
                    .filter(v -> v.getSemester() != null && v.getSemester().trim().toLowerCase().equals(selectedSemester))
                    .toList();
        }

        veranstaltungen.forEach(v ->
                v.getTermine().forEach(t ->
                        gruppenarbeitList.addAll(t.getGruppenarbeiten())
                )
        );


        if (filterText.getValue() == null || filterText.getValue().isEmpty()) {
            gruppenarbeitList.sort((g1, g2) -> g1.getTitel().compareTo(g2.getTitel()));
            gruppenarbeitenGrid.setItems(gruppenarbeitList.stream().distinct().toList());
        } else {
            String search = filterText.getValue().toLowerCase();
            List<Gruppenarbeit> resultList = new ArrayList<>();

            gruppenarbeitList.stream()
                    .filter(g -> g.getTitel().toLowerCase().contains(search))
                    .forEach(resultList::add);

            gruppenarbeitList.stream()
                    .filter(g -> FormatService.formatDate(g.getTermin().getDatum()).toLowerCase().contains(search))
                    .forEach(resultList::add);

            gruppenarbeitList.stream()
                    .filter(g -> g.getTermin().getVeranstaltung().getTitel().toLowerCase().contains(search))
                    .forEach(resultList::add);

            resultList.sort((g1, g2) -> g1.getTitel().compareTo(g2.getTitel()));

            gruppenarbeitenGrid.setItems(resultList.stream().distinct().toList());
        }
    }


    private Component getToolbar() {
        filterText.setPlaceholder("Suche");
        filterText.setClearButtonVisible(true);
        filterText.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(event -> {
            populateGrid();
        });

        semesterComboBox.setClearButtonVisible(true);
        semesterComboBox.setItems(semesterService.getMomSemesters());
        semesterComboBox.setPlaceholder("Semester");
        semesterComboBox.addValueChangeListener(event -> {
            populateGrid();
        });

        veranstaltungComboBox.setClearButtonVisible(true);
        veranstaltungComboBox.setItemLabelGenerator(Veranstaltung::getTitel);
        veranstaltungComboBox.setItems(benutzer.getVeranstaltungen());
        veranstaltungComboBox.setItems(benutzer.getVeranstaltungen());
        veranstaltungComboBox.setPlaceholder("Veranstaltung");
        veranstaltungComboBox.addValueChangeListener(event -> {
                    populateGrid();
        });

        HorizontalLayout toolbar = new HorizontalLayout(semesterComboBox,veranstaltungComboBox, filterText);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        return toolbar;
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

}
