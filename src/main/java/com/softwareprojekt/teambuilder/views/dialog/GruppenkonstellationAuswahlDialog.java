package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import com.softwareprojekt.teambuilder.services.TeilnahmeService;
import com.softwareprojekt.teambuilder.services.TerminService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
//Author: Thenujan Karunakumar
public class GruppenkonstellationAuswahlDialog extends Dialog {

    private final GruppenarbeitService gruppenarbeitService;
    private final TerminService terminService;
    private final TeilnahmeService teilnahmeService;
    private final long terminId;
    private Consumer<Gruppenarbeit> auswahlCallback;

    public GruppenkonstellationAuswahlDialog(GruppenarbeitService gruppenarbeitService,
                                             long terminId,
                                             Gruppenarbeit aktuelleGruppenarbeit,
                                             TerminService terminService,
                                             TeilnahmeService teilnahmeService) {
        this.gruppenarbeitService = gruppenarbeitService;
        this.terminId = terminId;
        this.terminService = terminService;
        this.teilnahmeService = teilnahmeService;

        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("600px");

        H1 title = new H1("Gruppenkonstellation übernehmen");

        ComboBox<Gruppenarbeit> gruppenarbeitBox = new ComboBox<>("Gruppenarbeit auswählen");
        gruppenarbeitBox.setItemLabelGenerator(Gruppenarbeit::getTitel);
        gruppenarbeitBox.setWidthFull();

        List<Gruppenarbeit> gruppenarbeiten = gruppenarbeitService.findByTermin(terminService.findTerminById(terminId));

        gruppenarbeitBox.setItems(
                gruppenarbeiten.stream()
                        .filter(g -> !Objects.equals(g.getId(), aktuelleGruppenarbeit.getId()))
                        .toList()
        );

        if (gruppenarbeitBox.getDataProvider().size(new com.vaadin.flow.data.provider.Query<>()) == 0) {
            Notification.show("Keine andere Gruppenarbeit mit diesem Termin gefunden.", 4000, Notification.Position.MIDDLE);
        }

        Div infoText = new Div();
        infoText.setText("Nur Gruppenarbeiten mit demselben Termin werden angezeigt.");
        infoText.getStyle()
                .set("fontSize", "var(--lumo-font-size-s)")
                .set("color", "gray")
                .set("marginBottom", "1em");

        Button uebernehmenButton = new Button("Übernehmen", VaadinIcon.CHECK.create());
        Button abbrechenButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());

        uebernehmenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        abbrechenButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        uebernehmenButton.addClickListener(e -> {
            Gruppenarbeit auswahl = gruppenarbeitBox.getValue();
            if (auswahl == null) {
                Notification.show("Bitte wählen Sie eine Gruppenarbeit aus.", 3000, Notification.Position.MIDDLE);
                return;
            }

            Set<Long> aktuelleTeilnehmerIds = teilnahmeService.findeAlleByGruppenarbeit(aktuelleGruppenarbeit)
                    .stream().map(t -> t.getTeilnehmer().getMatrnr()).collect(Collectors.toSet());

            Set<Long> ausgewaehlteTeilnehmerIds = teilnahmeService.findeAlleByGruppenarbeit(auswahl)
                    .stream().map(t -> t.getTeilnehmer().getMatrnr()).collect(Collectors.toSet());


            if (!aktuelleTeilnehmerIds.equals(ausgewaehlteTeilnehmerIds)&& !aktuelleTeilnehmerIds.isEmpty()) {
                zeigeWarnDialog(auswahl);
            } else {
                // Direkt übernehmen
                if (auswahlCallback != null) {
                    auswahlCallback.accept(auswahl);
                }
                close();
            }
        });

        abbrechenButton.addClickListener(e -> close());

        add(title, infoText, gruppenarbeitBox, new HorizontalLayout(uebernehmenButton, abbrechenButton));
    }

    public void setAuswahlCallback(Consumer<Gruppenarbeit> callback) {
        this.auswahlCallback = callback;
    }

    private void zeigeWarnDialog(Gruppenarbeit gruppenarbeit) {
        Dialog warnDialog = new Dialog();
        warnDialog.setModal(true);
        warnDialog.setDraggable(false);
        warnDialog.setResizable(false);
        warnDialog.setWidth("500px");

        H1 warnTitle = new H1("Achtung: Teilnehmerabweichung");
        Div warnText = new Div();
        warnText.setText("Die Teilnehmer dieser Gruppenarbeit unterscheiden sich von der aktuellen. Fortfahren?");
        warnText.getStyle().set("marginBottom", "1em");

        Button trotzdemButton = new Button("Trotzdem übernehmen", e -> {
            if (auswahlCallback != null) {
                auswahlCallback.accept(gruppenarbeit);
            }
            warnDialog.close();
            close();
        });

        Button abbrechenButton = new Button("Abbrechen", e -> warnDialog.close());

        trotzdemButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        abbrechenButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(trotzdemButton, abbrechenButton);

        warnDialog.add(warnTitle, warnText, buttons);
        warnDialog.open();
    }
}
