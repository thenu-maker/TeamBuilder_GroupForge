package com.softwareprojekt.teambuilder.views.gruppenarbeiten;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.GruppeService;
import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import com.softwareprojekt.teambuilder.services.TeilnahmeService;
import com.softwareprojekt.teambuilder.services.TerminService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.GruppenkonstellationAuswahlDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

//Author: Thenujan Karunakumar
@Route(value = "gruppenarbeit/:gruppenarbeitid", layout = MainLayout.class)
@PageTitle("AktuelleGruppenarbeit | GroupForge")
@PermitAll
public class AktuelleGruppenarbeitView extends VerticalLayout  implements BeforeEnterObserver {

    private final GruppenarbeitService gruppenarbeitService;
    private final GruppeService gruppeService;
    private final TerminService terminService;
    private final TeilnahmeService teilnahmeService;

    private Gruppenarbeit aktuelleGruppenarbeit;
    private final Grid<Teilnehmer> grid = new Grid<>();
    private final Button zurueckButton= new Button("Zurück zur Übersicht");
    private Long gruppenarbeitId;
    private final Button konstellationDialogButton = new Button("Konstellation übernehmen");



    Select<Integer> gruppenanzahl = new Select<>();

    public AktuelleGruppenarbeitView(GruppenarbeitService gruppenarbeitService,
                                     GruppeService gruppeService,
                                     TerminService terminService,
                                     TeilnahmeService teilnahmeService) {

        this.gruppenarbeitService = gruppenarbeitService;
        this.gruppeService = gruppeService;
        this.terminService = terminService;
        this.teilnahmeService = teilnahmeService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        grid.addColumn(Teilnehmer::getMatrnr).setHeader("Matrikelnummer");
        grid.addColumn(Teilnehmer::getVorname).setHeader("Vorname");
        grid.addColumn(Teilnehmer::getNachname).setHeader("Nachname");
        String gruppenarbeitIdStr= event.getRouteParameters().get("gruppenarbeitid").orElse(null);

        if (gruppenarbeitIdStr==null) {
            throw new IllegalArgumentException("keine Gruppenarbeit ID angegeben");
        }

        try{
            gruppenarbeitId = Long.parseLong(gruppenarbeitIdStr);
            aktuelleGruppenarbeit = gruppenarbeitService.findGruppenarbeitById(gruppenarbeitId);
            if (aktuelleGruppenarbeit == null) {
                throw new IllegalArgumentException("Gruppenarbeit nicht gefunden");

            }

            zurueckButton.addClickListener(e ->
                    UI.getCurrent().navigate("termin/" + aktuelleGruppenarbeit.getTermin().getId()));
            konstellationDialogButton.addClickListener(e -> {
                GruppenkonstellationAuswahlDialog dialog = new GruppenkonstellationAuswahlDialog(
                        gruppenarbeitService,
                        aktuelleGruppenarbeit.getTermin().getId(), aktuelleGruppenarbeit, terminService, teilnahmeService
                );

                dialog.setAuswahlCallback(gewaehlteGruppenarbeit -> {
                    try {
                        if (gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).isEmpty()) {
                            gruppenarbeitService.uebernehmeGruppenVon(gewaehlteGruppenarbeit, aktuelleGruppenarbeit);
                            UI.getCurrent().navigate("gruppenarbeit/" + aktuelleGruppenarbeit.getId() + "/losen?alteKonst=true");
                        } else {
                            Notification.show("Es existieren bereits Gruppen in dieser Gruppenarbeit.", 3000, Notification.Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    } catch (Exception ex) {
                        Notification.show("Fehler: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });

                dialog.open();
            });


            buildView();


        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());

        }
    }

        private void buildView() {
        removeAll();
        add(zurueckButton);

        add(new H1("Aktuelle Gruppenarbeit – " + aktuelleGruppenarbeit.getTitel() + " - Teilnehmer"));
        gruppenanzahl.setItems(gruppenarbeitService.berechneSinnvolleGruppenzahlen(aktuelleGruppenarbeit.getTermin().getTeilnehmer().size()));



        Button losenButton = new Button("Losen");
        losenButton.setWidth("200px");
        losenButton.addThemeName("primary");
        losenButton.addClickListener(e -> losen());
        grid.setItems(aktuelleGruppenarbeit.getTermin().getTeilnehmer());
            add(new HorizontalLayout(gruppenanzahl, konstellationDialogButton));

            add(grid);
        add(losenButton);
    }

    private void losen() {
        try {

            UI.getCurrent().navigate("gruppenarbeit/"+
                    aktuelleGruppenarbeit.getId() +
                    "/losen?anzahl=" + gruppenanzahl.getValue()
            );
        } catch (Exception ex) {
            Notification.show(
                    "Fehler beim Auslosen: " + ex.getMessage(),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void alteKonstellationUebernehmen() {
        try {
            if (gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).isEmpty()) {
                Gruppenarbeit alteGruppenarbeit = gruppenarbeitService.getVorherigeGruppenarbeit(aktuelleGruppenarbeit);
                if ( alteGruppenarbeit!= null) {
                    gruppenarbeitService.uebernehmeAlteKonstellation(aktuelleGruppenarbeit, alteGruppenarbeit);
                    UI.getCurrent().navigate("gruppenarbeit/"+
                            aktuelleGruppenarbeit.getId() +
                            "/losen?alteKonst=" + true);
                }
            }

        } catch (Exception e) {
            Notification.show("Fehler beim Übernehmen der alten Konstellation: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    }







