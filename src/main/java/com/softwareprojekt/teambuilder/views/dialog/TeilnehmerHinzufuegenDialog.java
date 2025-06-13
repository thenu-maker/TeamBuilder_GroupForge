package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.data.domain.PageRequest;
//Author: Tolga Cenk Kilic
public class TeilnehmerHinzufuegenDialog extends AbstractTeilnehmerDialog {
    public TeilnehmerHinzufuegenDialog(TeilnehmerService teilnehmerService) {
        super(teilnehmerService);
        initDialog();
    }

    private void initDialog() {
        initCommonLayout("Teilnehmer Hinzufügen");
        aehnlicheFelderKonfigurieren(null);

        // Search ComboBox
        ComboBox<Teilnehmer> searchField = new ComboBox<>("Teilnehmer suchen");
        searchField.setPlaceholder("MatrNr, Vorname oder Nachname…");
        searchField.setItemLabelGenerator(t -> t.getMatrnr() + " - " + t.getVorname() + " " + t.getNachname());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();

        searchField.addCustomValueSetListener(e ->
                Notification.show("Kein Teilnehmer gefunden: " + e.getDetail(), 3000, Notification.Position.MIDDLE)
        );

        searchField.setItems(query -> {
            int page = query.getOffset() / query.getLimit();
            return teilnehmerService.findAllTeilnehmer(PageRequest.of(page, query.getLimit()))
                    .stream()
                    .filter(t -> String.valueOf(t.getMatrnr()).contains(query.getFilter().orElse("")) ||
                            t.getVorname().toLowerCase().contains(query.getFilter().orElse("").toLowerCase()) ||
                            t.getNachname().toLowerCase().contains(query.getFilter().orElse("").toLowerCase()));
        });

        searchField.addValueChangeListener(e -> {
            Teilnehmer selected = e.getValue();
            if (selected != null) {
                matrnrField.setValue(String.valueOf(selected.getMatrnr()));
                vornameField.setValue(selected.getVorname());
                nachnameField.setValue(selected.getNachname());
            }
        });

        matrnrField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && value.matches(MATRNR_REGEX)) {
                Teilnehmer teilnehmer = teilnehmerService.findTeilnehmer(Long.parseLong(value));
                if (teilnehmer != null) {
                    vornameField.setValue(teilnehmer.getVorname());
                    nachnameField.setValue(teilnehmer.getNachname());
                    Notification.show("Bestehender Teilnehmer wurde gefunden");
                }
            }
        });

        Button saveButton = new Button("Hinzufügen", VaadinIcon.CHECK.create());
        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());

        saveButton.addClickListener(e -> {
            if (felderUeberpruefung()) {
                Teilnehmer teilnehmer = new Teilnehmer();
                teilnehmer.setMatrnr(Long.parseLong(matrnrField.getValue()));
                teilnehmer.setVorname(vornameField.getValue());
                teilnehmer.setNachname(nachnameField.getValue());
                if (saveCallback != null) saveCallback.accept(teilnehmer);
                close();
            }
        });

        cancelButton.addClickListener(e -> close());

        VerticalLayout matrnrLayout = new VerticalLayout(matrnrField);
        matrnrLayout.setPadding(false);
        matrnrLayout.setSpacing(false);
        matrnrLayout.setWidthFull();

        HorizontalLayout namenLayout = new HorizontalLayout(vornameField, nachnameField);
        namenLayout.setWidthFull();
        namenLayout.setSpacing(true);
        namenLayout.setPadding(false);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);
        VerticalLayout content = new VerticalLayout(searchField, matrnrLayout, namenLayout, buttonLayout);

        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        add(content);
    }
}