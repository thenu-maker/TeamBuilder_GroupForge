package com.softwareprojekt.teambuilder.views.uebersicht;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.FormatService;
import com.softwareprojekt.teambuilder.services.VeranstaltungService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Author: Silas Weber
@Route(value = "/", layout = MainLayout.class)
@PageTitle("Dashboard | GroupForge")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final SecurityService securityService;
    private final VeranstaltungService veranstaltungService;

    private final Grid<Termin> veranstaltungsGrid = new Grid<>(Termin.class, false);
    H1 welcomer = new H1();
    H1 ueberschriftVeranstaltungen = new H1("Kommende Veranstaltungen:");


    public DashboardView(BenutzerService benutzerService,
                         SecurityService securityService,
                         VeranstaltungService veranstaltungService) {
        this.benutzerService = benutzerService;
        this.securityService = securityService;
        this.veranstaltungService = veranstaltungService;

        setSizeFull();
        configureWelcomer();
        configureGrid();

        add(
                welcomer,
                ueberschriftVeranstaltungen,
                veranstaltungsGrid
        );

        populateGrid();
    }

    private void configureWelcomer() {
        welcomer.setSizeFull();
        welcomer.getStyle().set("font-size", "var(--lumo-font-size-xxl)");
        welcomer.getStyle().set("font-weight", "bold");
        welcomer.getStyle().set("color", "var(--lumo-primary-text-color)");
        welcomer.getStyle().set("padding", "10px");
        welcomer.setHeight("100px");

        Benutzer benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());

        if(benutzer.getTitel() != null && !benutzer.getTitel().isEmpty())
        {
            welcomer.setText("Willkommen, " + benutzer.getTitel() + " " + benutzer.getVorname() + " " + benutzer.getNachname() + "!");
        }
        else {
            welcomer.setText("Willkommen, " + benutzer.getVorname() + " " + benutzer.getNachname() + "!");
        }
    }

    private void configureGrid() {
        veranstaltungsGrid.setSizeFull();
        veranstaltungsGrid.addColumn(e -> e.getVeranstaltung().getTitel())
                .setHeader("Titel")
                .setAutoWidth(true)
                .setSortable(true)

                .setFlexGrow(4);


        veranstaltungsGrid.addColumn(e -> e.getStartzeit().toString())
                .setHeader("Von")
                .setAutoWidth(true)
                .setFlexGrow(1);


        veranstaltungsGrid.addColumn(e -> e.getEndzeit().toString())
                .setHeader("Bis")
                .setAutoWidth(true)
                .setFlexGrow(1);

        veranstaltungsGrid.addColumn(e -> FormatService.formatDate(e.getDatum()))
                .setHeader("Datum")
                .setAutoWidth(true)
                .setSortable(true)

                .setFlexGrow(2);



        veranstaltungsGrid.addItemDoubleClickListener(event -> {
            Termin termin = event.getItem();
            UI.getCurrent().navigate("veranstaltung/" + termin.getVeranstaltung().getId() + "/");
        });


        veranstaltungsGrid.getElement().getStyle().set("user-select", "none");


        veranstaltungsGrid.setTooltipGenerator(termin -> "Doppelklick zum Öffnen der Veranstaltung \"" + termin.getVeranstaltung().getTitel() + "\"");

        ueberschriftVeranstaltungen.addClickListener(e -> {
            UI.getCurrent().navigate("veranstaltungen");
        });


        ueberschriftVeranstaltungen.addClassNames("text-xl");
    }

    private void populateGrid() {
        List<Termin> termine = new ArrayList<>();
        Benutzer benutzer = benutzerService.findBenutzerByUsername(securityService.getAuthenticatedUser().getUsername());
        List<Veranstaltung> veranstaltungen = benutzer.getVeranstaltungen();
        veranstaltungen.forEach(veranstaltung -> termine.add(
                veranstaltung.getTermine().stream()
                        .filter(termin -> FormatService.formatDate(termin.getDatum()).equals(veranstaltungService.getNaechsterTermin(veranstaltung.getId()))).findFirst().orElse(null))
        );

        termine.removeIf(Objects::isNull);

        termine.sort((t1, t2) -> t1.getDatum().compareTo(t2.getDatum()));

        termine.removeIf(termin ->termin.getDatum().isBefore(LocalDate.now()));
        termine.removeIf(termin ->termin.getDatum().isEqual(LocalDate.now()) && termin.getEndzeit().isAfter(LocalTime.now()));

        veranstaltungsGrid.setItems(termine);
    }




}
