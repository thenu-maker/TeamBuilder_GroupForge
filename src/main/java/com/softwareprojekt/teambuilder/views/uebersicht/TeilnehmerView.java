package com.softwareprojekt.teambuilder.views.uebersicht;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.SemesterService;
import com.softwareprojekt.teambuilder.services.TeilnahmeService;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerBearbeitenDialog;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

//Author: Silas Weber
@Route(value = "/teilnehmer", layout = MainLayout.class)
@PageTitle("Teilnehmerübersicht | GroupForge")
@PermitAll
public class TeilnehmerView extends VerticalLayout {

    private final TeilnahmeService teilnahmeService;
    private final SecurityService securityService;
    private final BenutzerService benutzerService;
    private final TeilnehmerService teilnehmerService;
    private final SemesterService semesterService;
    private Benutzer benutzer;


    Grid<Teilnehmer> teilnehmerGrid = new Grid<>(Teilnehmer.class, false);
    TextField filterText= new TextField();
    ComboBox<String> semesterComboBox = new ComboBox<>();

    public TeilnehmerView(SecurityService securityService,
                          BenutzerService benutzerService,
                          TeilnehmerService teilnehmerService,
                          TeilnahmeService teilnahmeService,
                          SemesterService semesterService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;
        this.teilnehmerService = teilnehmerService;
        this.teilnahmeService = teilnahmeService;
        benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());
        this.semesterService = semesterService;

        setSizeFull();

        configureGrid();

        add(new H1("Teilnehmerübersicht"));
        add(
                getToolbar(),
                teilnehmerGrid
        );

        populateGrid();
    }

    private void configureGrid() {
        teilnehmerGrid.setSizeFull();

        teilnehmerGrid.addColumn(Teilnehmer::getMatrnr)
            .setHeader("Matrikelnummer")
            .setSortable(true)
            .setFlexGrow(2);

        teilnehmerGrid.addColumn(Teilnehmer::getNachname)
            .setHeader("Nachname")
            .setSortable(true)
            .setFlexGrow(2);

        teilnehmerGrid.addColumn(Teilnehmer::getVorname)
            .setHeader("Vorname")
            .setSortable(true)
            .setFlexGrow(2);

        teilnehmerGrid.addColumn(t -> t.getTeilnahmen().stream()
                .map(Teilnahme::getPraesentationspunkte)
                .reduce(0.0, (a, b) -> a + b)
            )
            .setHeader("Summe P-Punkte")
            .setSortable(true)
            .setFlexGrow(1);

        teilnehmerGrid.addColumn(t -> t.getTeilnahmen().stream()
                .map(Teilnahme::getLeistungspunkte)
                .reduce(0.0, (a, b) -> a + b)
            )
            .setHeader("Summe L-Punkte")
            .setSortable(true)
            .setFlexGrow(1);


        GridContextMenu<Teilnehmer> contextMenu = new GridContextMenu<>(teilnehmerGrid);

        // Bearbeiten-Menüpunkt mit Icon
        contextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.EDIT), new Span("Bearbeiten")),
                        event -> {
                            event.getItem().ifPresent(teilnehmer -> {
                                TeilnehmerBearbeitenDialog bearbeitenDialog = new TeilnehmerBearbeitenDialog(teilnehmerService, teilnehmer);
                                bearbeitenDialog.setSaveCallback(geänderterTeilnehmer -> populateGrid());
                                bearbeitenDialog.open();
                            });
                        }
                );


        // Löschen-Menüpunkt mit Icon
        contextMenu.addItem(
                new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Löschen")),
                        this::deleteTeilnehmerEvent
                );

        teilnehmerGrid.setTooltipGenerator(t ->
                "Rechtsklick zum Bearbeiten oder Löschen von " + t.getMatrnr() + " " + t.getNachname() + " " + t.getVorname()
        );
    }

    private void populateGrid() {
        List<Teilnehmer> teilnehmerList = new ArrayList<>();

        Benutzer benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());
        List<Veranstaltung> veranstaltungen = benutzer.getVeranstaltungen();

        if (semesterComboBox.getValue() != null)
        {
            veranstaltungen.stream().filter(v ->
                            v.getSemester()
                                    .toLowerCase()
                                    .trim()
                                    .equals(semesterComboBox.getValue()
                                            .toLowerCase()
                                            .trim()))
                    .forEach(v -> teilnehmerList.addAll(v.getTeilnehmer()));
        }
        else {
            veranstaltungen.forEach(v -> teilnehmerList.addAll(v.getTeilnehmer()));
        }

        if(filterText.getValue() == null || filterText.getValue().isEmpty()) {
            teilnehmerList.sort((t1, t2) -> t1.getNachname().compareTo(t2.getNachname()));
            teilnehmerGrid.setItems(teilnehmerList.stream().distinct().toList());
        }
        else {
            List<Teilnehmer> resultList = new ArrayList<>();

            teilnehmerList.stream().filter(t -> String.valueOf(t.getMatrnr()).contains(filterText.getValue())).forEach(resultList::add);
            teilnehmerList.stream().filter(t -> t.getNachname().toLowerCase().contains(filterText.getValue().toLowerCase())).forEach(resultList::add);
            teilnehmerList.stream().filter(t -> t.getVorname().toLowerCase().contains(filterText.getValue().toLowerCase())).forEach(resultList::add);

            resultList.sort((t1, t2) -> t1.getNachname().compareTo(t2.getNachname()));

            teilnehmerGrid.setItems(resultList.stream().distinct().toList());
        }
    }

    private Component getToolbar(){
        filterText.setPlaceholder("Suche");
        filterText.setClearButtonVisible(true);
        filterText.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(event -> {populateGrid();});

        semesterComboBox.setClearButtonVisible(true);
        semesterComboBox.setItems(semesterService.getMomSemesters());
        semesterComboBox.setPlaceholder("Semester");
        semesterComboBox.addValueChangeListener(event -> {populateGrid();});

        HorizontalLayout toolbar = new HorizontalLayout(semesterComboBox, filterText);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        return toolbar;
    }

    private void deleteTeilnehmerEvent(GridContextMenu.GridContextMenuItemClickEvent<Teilnehmer> event) {

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Teilnehmer löschen?");
        dialog.setText("Möchten Sie den Teilnehmer ("+ event.getItem().get().getVorname() + " " + event.getItem().get().getNachname() +") wirklich löschen?");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            event.getItem().ifPresent(teilnehmer -> {

                teilnehmerService.deleteTeilnehmerMitAbhaengigkeiten(teilnehmer);
                populateGrid();
                Notification.show("Teilnehmer entfernt.", 3000, Notification.Position.MIDDLE);
            });
        });

        dialog.open();
    }

}
