package com.softwareprojekt.teambuilder.views.gruppenarbeiten;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.*;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.softwareprojekt.teambuilder.views.dialog.GruppenkonstellationAuswahlDialog;
import com.softwareprojekt.teambuilder.views.dialog.TeilnehmerHinzufuegenDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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

import java.util.*;
import java.util.stream.Collectors;

//Author: Thenujan Karunakumar
@Route(value = "gruppenarbeit/:gruppenarbeitid/losen", layout = MainLayout.class)
@PageTitle("Gruppen losen  | GroupForge")
@PermitAll
public class GruppenarbeitGruppenLosenView extends VerticalLayout implements BeforeEnterObserver {

    private final GruppenarbeitService gruppenarbeitService;
    private final TeilnahmeService teilnahmeService;
    private final GruppeService gruppeService;
    private final TeilnehmerService teilnehmerService;
    private final TerminService terminService;

    private Gruppenarbeit aktuelleGruppenarbeit;
    private final Select<Integer> gruppenanzahl = new Select<>();
    private final Button losenButton = new Button("Losen");
    //    private final Button verwerfenButton = new Button("Alles löschen");
    private final Button zurueckButton = new Button("Zurück zur Übersicht");
    private final Span statusLabel = new Span("Gruppen wurden gespeichert.");


    private final Grid<Teilnahme> grid = new Grid<>(Teilnahme.class);
    private final Checkbox toggleDragDropView = new Checkbox("Tabelle");
    private final Div dragDropLayout = new Div();

    private int anzahl;
    private List<Teilnahme> urspruenglicheTeilnahmen;
    private boolean hatteVorherGruppen = false;
    private final Button wiederherstellenButton = new Button("Wiederherstellen");


    public GruppenarbeitGruppenLosenView(GruppenarbeitService gruppenarbeitService,
                                         TeilnahmeService teilnahmeService,
                                         GruppeService gruppeService,
                                         TeilnehmerService teilnehmerService,
                                         TerminService terminService) {

        this.gruppenarbeitService = gruppenarbeitService;
        this.teilnahmeService = teilnahmeService;
        this.gruppeService = gruppeService;
        this.teilnehmerService = teilnehmerService;
        this.terminService = terminService;
    }

    private void buildView() {

        losenButton.addClickListener(e -> losenGruppen());
        losenButton.addThemeName("primary");

        statusLabel.getStyle().set("color", "green");
        statusLabel.getStyle().set("fontWeight", "bold");

        grid.removeAllColumns();
        grid.addColumn(teilnahme -> teilnahme.getGruppe().getGruppenname()).setHeader("Gruppe").setAutoWidth(true).setFlexGrow(1);;
        grid.addColumn(teilnahme -> teilnahme.getTeilnehmer().getVorname() + " " + teilnahme.getTeilnehmer().getNachname()).setHeader("Teilnehmer").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(teilnahme -> teilnahme.getGruppe().getAnmerkung()).setHeader("Anmerkung").setAutoWidth(true).setFlexGrow(4);
        grid.setSizeFull();
        grid.setAllRowsVisible(true);

        toggleDragDropView.setValue(false); // drag-and-drop ist Default
        toggleDragDropView.addValueChangeListener(e -> toggleAnsicht(e.getValue()));


        grid.setVisible(false);
        dragDropLayout.setVisible(true);
        renderDragAndDropView();

        wiederherstellenButton.addClickListener(e -> {
            if (urspruenglicheTeilnahmen != null) {
                teilnahmeService.deleteAllByGruppenarbeit(aktuelleGruppenarbeit);
                gruppeService.loescheLeereGruppen(aktuelleGruppenarbeit);

                Map<String, Gruppe> gruppenMap = new HashMap<>();

                for (Teilnahme t : urspruenglicheTeilnahmen) {
                    String gruppenname = t.getGruppe().getGruppenname();

                    if (!gruppenMap.containsKey(gruppenname)) {
                        Gruppe neueGruppe = new Gruppe();
                        neueGruppe.setGruppenname(gruppenname);
                        neueGruppe.setGruppenarbeit(aktuelleGruppenarbeit);
                        gruppeService.save(neueGruppe);

                        gruppenMap.put(gruppenname, neueGruppe);
                    }


                }



                List<Teilnahme> neueTeilnahmen = urspruenglicheTeilnahmen.stream()
                        .map(t -> {
                            Teilnahme kopie = new Teilnahme();
                            kopie.setGruppe(gruppenMap.get(t.getGruppe().getGruppenname()));
                            kopie.setTeilnehmer(t.getTeilnehmer());
                            return kopie;
                        })
                        .collect(Collectors.toList());

                gruppenarbeitService.speichereTeilnahmenMitGruppen(neueTeilnahmen);


                urspruenglicheTeilnahmen = neueTeilnahmen.stream()
                        .map(t -> {
                            Teilnahme kopie = new Teilnahme();
                            kopie.setGruppe(t.getGruppe());
                            kopie.setTeilnehmer(t.getTeilnehmer());
                            return kopie;
                        })
                        .collect(Collectors.toList());

                Notification.show("Gruppen wurden wiederhergestellt.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                if (!toggleDragDropView.getValue()) {
                    renderDragAndDropView();
                } else {
                    grid.setItems(teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit));
                }
            }
        });
        wiederherstellenButton.setVisible(hatteVorherGruppen);

        Button addTeilnehmerButton = new Button("Teilnehmer hinzufügen", VaadinIcon.PLUS.create());
        addTeilnehmerButton.addClickListener(e -> {
            TeilnehmerHinzufuegenDialog dialog = new TeilnehmerHinzufuegenDialog(teilnehmerService);

            dialog.setSaveCallback(this::hinzufuegenTeilnehmer);

            dialog.open();

            gruppenanzahl.setItems(gruppenarbeitService.berechneSinnvolleGruppenzahlen(
                    aktuelleGruppenarbeit.getTermin().getTeilnehmer().size()));


        });

        Button vorlageButton = new Button("Konstellation übernehmen");
        vorlageButton.addClickListener(e -> {
            GruppenkonstellationAuswahlDialog dialog = new GruppenkonstellationAuswahlDialog(gruppenarbeitService,
                    aktuelleGruppenarbeit.getTermin().getId(), aktuelleGruppenarbeit, terminService, teilnahmeService);

            dialog.setAuswahlCallback(gewaehlteGruppenarbeit -> {

                gruppenarbeitService.uebernehmeGruppenVon(gewaehlteGruppenarbeit, aktuelleGruppenarbeit);
                grid.setItems(teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit));
                renderDragAndDropView();
                Notification.show("Konstellation übernommen.");
            });

            dialog.open();
        });


        add(
                zurueckButton,
                new H1(aktuelleGruppenarbeit.getTitel() + " – Gruppen losen"),
                new HorizontalLayout(gruppenanzahl, losenButton, wiederherstellenButton, addTeilnehmerButton, vorlageButton, toggleDragDropView, statusLabel),
                grid,
                dragDropLayout
        );

        gruppenanzahl.setItems(gruppenarbeitService.berechneSinnvolleGruppenzahlen(
                aktuelleGruppenarbeit.getTermin().getTeilnehmer().size()));
        gruppenanzahl.setValue(anzahl);
    }


    private void toggleAnsicht(boolean tabellenansichtAktiv) {

        grid.setVisible(tabellenansichtAktiv);
        dragDropLayout.setVisible(!tabellenansichtAktiv);
        if (!tabellenansichtAktiv) {
            renderDragAndDropView();
        }
    }


    private void renderDragAndDropView() {
        dragDropLayout.removeAll();
        dragDropLayout.getStyle()
                .set("display", "flex")
                .set("flexWrap", "wrap")
                .set("gap", "1.5em")
                .set("justifyContent", "center");

        Map<String, List<Teilnahme>> gruppen = teilnahmeService
                .findeAlleByGruppenarbeit(aktuelleGruppenarbeit)
                .stream()
                .collect(Collectors.groupingBy(t -> t.getGruppe().getGruppenname()));

        List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
        GruppenKachelLayout layout = new GruppenKachelLayout(
                    teilnahmen,
                    true,
                    (id, gruppe) -> gruppenarbeitService.aendereGruppeVonTeilnahme(id, gruppe),
                    (gruppenId, neueAnmerkung) -> gruppeService.aendereAnmerkungVonGruppe(gruppenId, neueAnmerkung)
                );
        gruppenanzahl.setItems(gruppenarbeitService.berechneSinnvolleGruppenzahlen(
                aktuelleGruppenarbeit.getTermin().getTeilnehmer().size()));
        gruppenanzahl.setValue(anzahl);
        dragDropLayout.removeAll();
        dragDropLayout.add(layout);

    }


    private void losenGruppen() {
        try {
            statusLabel.setText("Wird gelost...");
            if (gruppenanzahl.getValue() != null && gruppenanzahl.getValue() > 0) {
                anzahl = gruppenanzahl.getValue();
                if (gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).isEmpty()) {
                    gruppenarbeitService.erstelleGruppenNachAnzahl(aktuelleGruppenarbeit, anzahl);
                } else {
                    List<Gruppe> leereGruppen = gruppenarbeitService.erstelleLeereGruppenNachAnzahl(aktuelleGruppenarbeit, anzahl);
                    //gehe die alten Teilnahmen durch und tausche die GruppenId
                    List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
                    int currentGruppe = 0;

                    for (Teilnahme teilnahme : teilnahmen) {
                        Gruppe alteGruppe = teilnahme.getGruppe();
                        Gruppe gruppe = leereGruppen.get(currentGruppe);

                        teilnahme.setGruppe(gruppe);

                        gruppe.addTeilnahme(teilnahme);


                        currentGruppe = (currentGruppe + 1) % anzahl;

                    }
                    teilnahmeService.saveAll(teilnahmen);
                    gruppeService.loescheLeereGruppen(aktuelleGruppenarbeit);

                }
                List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
                grid.setItems(teilnahmen);
                if (!toggleDragDropView.getValue()) {
                    renderDragAndDropView();
                }
            }
            statusLabel.setText("Gruppen wurden gespeichert.");
            anzahl = gruppenanzahl.getValue();

        } catch (Exception ex) {
            Notification.show("Fehler beim Auslosen: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

    }

    private void hinzufuegenTeilnehmer(Teilnehmer teilnehmer) {
        if (aktuelleGruppenarbeit.getTermin().getTeilnehmer().stream()
                .anyMatch(t -> Objects.equals(t.getMatrnr(), teilnehmer.getMatrnr()))) {
            Notification.show("Teilnehmer bereits vorhanden!", 3000, Notification.Position.MIDDLE);
            return;
        }

        Gruppe neueGruppe = new Gruppe();
        neueGruppe.setGruppenarbeit(aktuelleGruppenarbeit);
        neueGruppe.setGruppenname("Gruppe " + (gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).size() + 1));
        gruppeService.save(neueGruppe);

        Teilnahme neueTeilnahme = new Teilnahme();
        neueTeilnahme.setTeilnehmer(teilnehmer);
        neueTeilnahme.setGruppe(neueGruppe);
        teilnahmeService.save(neueTeilnahme);

        terminService.addTeilnehmerToTermin(
                terminService.findTerminById(aktuelleGruppenarbeit.getTermin().getId()),
                teilnehmer
        );

        grid.setItems(teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit));
        renderDragAndDropView();
    }

    private int extractGroupNumber(String gruppenname) {
        try {
            return Integer.parseInt(gruppenname.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String gruppenarbeitIdStr = event.getRouteParameters().get("gruppenarbeitid").orElse(null);
        String altKonstStr = event.getRouteParameters().get("alteKonst").orElse(null);


        if (gruppenarbeitIdStr == null) {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }


        try {
            Long gruppenarbeitId = Long.parseLong(gruppenarbeitIdStr);
            aktuelleGruppenarbeit = gruppenarbeitService.findGruppenarbeitById(gruppenarbeitId);
            if (aktuelleGruppenarbeit == null) {
                throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

            }

            if (altKonstStr != null) {
                boolean altKonst = Boolean.parseBoolean(altKonstStr);
                if (altKonst) {
                    List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
                    grid.setItems(teilnahmen);
                    buildView();
                }

            }

            if (gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).isEmpty()) {
                zurueckButton.addClickListener(e -> {
                    UI.getCurrent().navigate("gruppenarbeit/" + aktuelleGruppenarbeit.getId() + "/anzeige");
                    gruppeService.loescheLeereGruppen(aktuelleGruppenarbeit);
                });

                Optional<String> anzahlOpt = event.getLocation().getQueryParameters()
                        .getParameters().getOrDefault("anzahl", List.of()).stream().findFirst();


                if (anzahlOpt.isPresent()) {
                    try {
                        anzahl = Integer.parseInt(anzahlOpt.get());
                        gruppenarbeitService.loescheVorhandeneGruppen(aktuelleGruppenarbeit);
                        gruppenarbeitService.erstelleGruppenNachAnzahl(aktuelleGruppenarbeit, anzahl);
                    } catch (NumberFormatException e) {
                        add(new H1("Ungültiger Parameter für Anzahl."));
                        return;
                    }

                } else {
                    anzahl = 1;
                    gruppenarbeitService.erstelleGruppenNachAnzahl(aktuelleGruppenarbeit, anzahl);
                }
                List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);
                grid.setItems(teilnahmen);
                buildView();

            } else {
                zurueckButton.addClickListener(e -> {
                    UI.getCurrent().navigate("gruppenarbeit/" + aktuelleGruppenarbeit.getId() + "/anzeige");
                    gruppeService.loescheLeereGruppen(aktuelleGruppenarbeit);//damit beim wieder bearbeiten die richtige Zahl bei dem dropdownSelect steht
                });


                anzahl = gruppeService.findAllByGruppenarbeit(aktuelleGruppenarbeit).size();
                List<Teilnahme> teilnahmen = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit);


                urspruenglicheTeilnahmen = teilnahmen.stream()
                        .map(t -> {
                            Gruppe neueGruppe = new Gruppe();
                            neueGruppe.setGruppenname(t.getGruppe().getGruppenname());
                            neueGruppe.setGruppenarbeit(aktuelleGruppenarbeit);

                            Teilnahme kopie = new Teilnahme();

                            kopie.setGruppe(neueGruppe);
                            kopie.setTeilnehmer(t.getTeilnehmer());
                            return kopie;
                        })
                        .collect(Collectors.toList());
//
                hatteVorherGruppen = true;
                grid.setItems(teilnahmen);
                buildView();

            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }
    }
}