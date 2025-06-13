package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.SemesterService;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
import com.softwareprojekt.teambuilder.services.VeranstaltungService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

//Author: Silas Weber
@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin-Page | GroupForge")
@RolesAllowed("ADMIN")
public class AdminView extends HorizontalLayout {

    TabSheet options = new TabSheet();
    private final BenutzerService benutzerService;
    private final TeilnehmerService teilnehmerService;
    private final VeranstaltungService veranstaltungService;
    private final SecurityService securityService;
    private BenutzerverwaltungComponent benutzerView;
    private TeilnehmerverwaltungComponent teilnehmerView;
    private VeranstaltungsVerwaltungComponent veranstaltungsView;
    private final SemesterService semesterService;

    public AdminView(BenutzerService benutzerService,
                     TeilnehmerService teilnehmerService,
                     VeranstaltungService veranstaltungService,
                     SecurityService securityService,
                     SemesterService semesterService) {
        this.benutzerService = benutzerService;
        this.teilnehmerService = teilnehmerService;
        this.veranstaltungService = veranstaltungService;
        this.securityService = securityService;
        this.semesterService = semesterService;

        setSizeFull();

        configureViews();

        createTabsheets();

        add(
                options
                //getToolbar()
        );

    }

    private void createTabsheets() {
        options.setSizeFull();
        options.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
        options.add("Benutzer-Verwaltung", benutzerView);
        options.add("Teilnehmer-Verwaltung", teilnehmerView);
        options.add("Veranstaltungs-Verwaltung", veranstaltungsView);
    }

    private void configureViews() {
        benutzerView  = new BenutzerverwaltungComponent(benutzerService, securityService);
        teilnehmerView = new TeilnehmerverwaltungComponent(teilnehmerService);
        veranstaltungsView = new VeranstaltungsVerwaltungComponent(veranstaltungService, benutzerService, securityService, semesterService);
    }

    private Component getToolbar() {
       VerticalLayout toolbar = new VerticalLayout();
       toolbar.setWidth("20%");

       H1 title = new H1("Toolbar");
       toolbar.add(title);

       return toolbar;
    }
}
