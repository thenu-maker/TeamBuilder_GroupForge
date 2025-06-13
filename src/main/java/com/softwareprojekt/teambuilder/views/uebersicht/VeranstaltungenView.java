package com.softwareprojekt.teambuilder.views.uebersicht;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.SemesterService;
import com.softwareprojekt.teambuilder.services.VeranstaltungService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
@Route(value = "veranstaltungen", layout = MainLayout.class)
@PageTitle("Veranstaltungsübersicht | GroupForge")
@PermitAll
public class VeranstaltungenView extends VerticalLayout {

    private final Button addButton = new Button("+ Hinzufügen");
    private final Grid<Veranstaltung> grid = new Grid<>(Veranstaltung.class);
    private final BenutzerService benutzerService;
    private final SecurityService securityService;
    private final SemesterService semesterService;
    private Benutzer benutzer;


    TextField filterText= new TextField();
    ComboBox<String> semesterComboBox = new ComboBox<>();

    private final VeranstaltungService veranstaltungService;

    public VeranstaltungenView(BenutzerService benutzerService,
                               SecurityService securityService,
                               VeranstaltungService veranstaltungService,
                               SemesterService semesterService
    ) {

        this.benutzerService = benutzerService;
        this.securityService = securityService;
        this.veranstaltungService = veranstaltungService;
        this.semesterService = semesterService;
        benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());


        setSizeFull();
        setPadding(true);
        setSpacing(true);


        Button zurueckButton = new Button("Zurück zum Dashboard");
        zurueckButton.addClickListener(e -> UI.getCurrent().navigate("/"));

        add(zurueckButton); // ganz oben


        add(new H1("Veranstaltungsübersicht"));


        try {
            grid.removeAllColumns();
            grid.addColumn(Veranstaltung::getTitel)
                    .setHeader("Titel")
                    .setSortable(true)
                    .setFlexGrow(3);

            grid.addColumn(Veranstaltung::getSemester)
                    .setHeader("Semester")
                    .setSortable(true)
                    .setFlexGrow(2);

            grid.addColumn(v -> v.getTeilnehmer() == null ? 0 : v.getTeilnehmer().size())
                    .setHeader("Teilnehmeranzahl")
                    .setSortable(true)
                    .setFlexGrow(1);

            grid.addColumn(v -> veranstaltungService.getNaechsterTermin(v.getId()))
                    .setHeader("Nächster Termin")
                    .setSortable(true)
                    .setFlexGrow(1);

            populateGrid();

        } catch (Exception e) {
            e.printStackTrace();
            add(new Span("Fehler beim Laden der Veranstaltungen: " + e.getMessage()));
        }


        grid.addItemDoubleClickListener(event -> UI.getCurrent().navigate("veranstaltung/" + event.getItem().getId()));

        grid.setTooltipGenerator(v -> "Doppelklick zum Öffnen der Veranstaltung: \"" + v.getTitel() + "\"");

        grid.getElement().getStyle().set("user-select", "none");


        add(getToolbar(), grid);
    }

    private void populateGrid() {
        List<Veranstaltung> veranstaltungList = new ArrayList<>();

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
                    .forEach(veranstaltungList::add);
        }
        else {
            veranstaltungList.addAll(veranstaltungen);
        }

        if(filterText.getValue() == null || filterText.getValue().isEmpty()) {
            veranstaltungList.sort((v1, v2) -> v1.getTitel().compareTo(v1.getTitel()));
            grid.setItems(veranstaltungList.stream().distinct().toList());
        }
        else {
            List<Veranstaltung> resultList = new ArrayList<>();

            veranstaltungList.stream().filter(v -> v.getTitel().toLowerCase().contains(filterText.getValue())).forEach(resultList::add);
            veranstaltungList.stream().filter(v -> String.valueOf(v.getTeilnehmer().size()).toLowerCase().contains(filterText.getValue().toLowerCase())).forEach(resultList::add);
            veranstaltungList.stream().filter(v -> veranstaltungService.getNaechsterTermin(v.getId()).toLowerCase().contains(filterText.getValue().toLowerCase())).forEach(resultList::add);

            resultList.sort((v1, v2) -> v1.getTitel().compareTo(v2.getTitel()));

            grid.setItems(resultList.stream().distinct().toList());
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

        addButton.addClickListener(e -> UI.getCurrent().navigate("veranstaltungenErstellen"));

        HorizontalLayout toolbar = new HorizontalLayout(semesterComboBox, filterText, addButton);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        return toolbar;
    }
}
