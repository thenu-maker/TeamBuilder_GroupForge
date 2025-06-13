package com.softwareprojekt.teambuilder.views.gruppenarbeiten;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import com.softwareprojekt.teambuilder.services.TeilnahmeService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Author: Thenujan Karunakumar
@Route(value = "gruppenarbeit/:id/anzeige", layout = MainLayout.class)
@PageTitle("Gruppen anzeigen | GroupForge")
@PermitAll
public class GruppenarbeitGruppenAnzeigeView extends VerticalLayout implements BeforeEnterObserver {

    private final GruppenarbeitService gruppenarbeitService;
    private final TeilnahmeService teilnahmeService;

    private final Button zurueckButton = new Button("Zurück zur Übersicht");
    private final Button bewertenButton = new Button("Gruppen bewerten");
    private final Checkbox toggleAnsichtCheckbox = new Checkbox("Tabelle");
    private final Grid<Teilnahme> grid = new Grid<>(Teilnahme.class, false);
    private final Div kachelLayout = new Div();
    private  Gruppenarbeit aktuelleGruppenarbeit;

    private H1 titel = new H1("Gruppenarbeit");
    private final HorizontalLayout bar = new HorizontalLayout();

    public GruppenarbeitGruppenAnzeigeView(GruppenarbeitService gruppenarbeitService,
                                           TeilnahmeService teilnahmeService) {
        this.gruppenarbeitService = gruppenarbeitService;
        this.teilnahmeService = teilnahmeService;

        bewertenButton.addThemeName("primary");

        bar.setWidthFull();
        bar.add(titel, toggleAnsichtCheckbox, bewertenButton);
        bar.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        bar.expand(titel);

        add(zurueckButton, bar);

        grid.addColumn(t -> t.getGruppe().getGruppenname()).setHeader("Gruppe").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(t -> t.getTeilnehmer().getVorname() + " " + t.getTeilnehmer().getNachname()).setHeader("Teilnehmer").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(t -> t.getGruppe().getAnmerkung()).setHeader("Anmerkung").setAutoWidth(true).setFlexGrow(4);
        grid.setSizeFull();
        grid.setAllRowsVisible(true);

        kachelLayout.getStyle().set("display", "flex").set("gap", "2em").set("flexWrap", "wrap");

        toggleAnsichtCheckbox.setValue(false); // Kachelansicht als Standard
        toggleAnsichtCheckbox.addValueChangeListener(e -> toggleAnsicht(e.getValue()));

        add(grid, kachelLayout);

        grid.setVisible(false);  // Start mit Kachelansicht
        kachelLayout.setVisible(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var routeParams = event.getRouteParameters();
        String gruppenarbeitIdStr = routeParams.get("id").orElse(null);

        if (gruppenarbeitIdStr == null) {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }

        try {
            Long gruppenarbeitId = Long.parseLong(gruppenarbeitIdStr);
            aktuelleGruppenarbeit = gruppenarbeitService.findGruppenarbeitById(gruppenarbeitId);
            if (aktuelleGruppenarbeit != null) {
                titel.setText("Gruppenarbeit: " + aktuelleGruppenarbeit.getTitel());
                var teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);

                teilnahmen.sort((t1, t2) -> t1.getGruppe().getGruppenname().compareTo(t2.getGruppe().getGruppenname()));

                grid.setItems(teilnahmen);
                renderStaticGruppenView(aktuelleGruppenarbeit);

                bewertenButton.addClickListener(e ->
                        UI.getCurrent().navigate("gruppenarbeit/" + aktuelleGruppenarbeit.getId() + "/bewerten"));

                Button bearbeitenButton = new Button("Gruppen bearbeiten");
                bearbeitenButton.addClickListener(e ->
                        UI.getCurrent().navigate("gruppenarbeit/" + aktuelleGruppenarbeit.getId() + "/losen"));
                add(bearbeitenButton);

                zurueckButton.addClickListener(e ->
                        UI.getCurrent().navigate("termin/" + aktuelleGruppenarbeit.getTermin().getId()));
            } else {
                throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }
    }

    private void toggleAnsicht(boolean tabellenAnsichtAktiv) {
        grid.setVisible(tabellenAnsichtAktiv);
        kachelLayout.setVisible(!tabellenAnsichtAktiv);
    }

    private void renderStaticGruppenView(Gruppenarbeit gruppenarbeit) {
        kachelLayout.removeAll();

        Map<String, List<Teilnahme>> gruppen = teilnahmeService.findeAlleByGruppenarbeit(gruppenarbeit)
                .stream()
                .collect(Collectors.groupingBy(t -> t.getGruppe().getGruppenname()));

        List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
        GruppenKachelLayout layout = new GruppenKachelLayout(teilnahmen, false, null, null);

        kachelLayout.add(layout);
    }

}
