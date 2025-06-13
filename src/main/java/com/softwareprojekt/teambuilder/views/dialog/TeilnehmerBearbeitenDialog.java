package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

//Author: Tolga Cenk Kilic
public class TeilnehmerBearbeitenDialog extends AbstractTeilnehmerDialog {
    private final Teilnehmer teilnehmer;

    public TeilnehmerBearbeitenDialog(TeilnehmerService teilnehmerService,
                                      Teilnehmer teilnehmer) {
        super(teilnehmerService);
        this.teilnehmer = teilnehmer;
        initDialog();
    }

    private void initDialog() {
        initCommonLayout("Teilnehmer Bearbeiten");
        aehnlicheFelderKonfigurieren(teilnehmer);
        matrnrField.setReadOnly(true);

        Button saveButton = new Button("Speichern", VaadinIcon.CHECK.create());
        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());

        saveButton.addClickListener(e -> {
            if (felderUeberpruefung()) {
                teilnehmer.setVorname(vornameField.getValue());
                teilnehmer.setNachname(nachnameField.getValue());
                teilnehmerService.saveTeilnehmer(teilnehmer);
                if (saveCallback != null) saveCallback.accept(teilnehmer);
                close();
            }
        });

        cancelButton.addClickListener(e -> close());


        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);


        VerticalLayout matrnrLayout = new VerticalLayout(matrnrField);
        matrnrLayout.setPadding(false);
        matrnrLayout.setSpacing(false);
        matrnrLayout.setWidth("100%");


        HorizontalLayout namenLayout = new HorizontalLayout(vornameField, nachnameField);
        namenLayout.setWidth("100%");
        namenLayout.setSpacing(true);
        namenLayout.setPadding(false);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);


        content.add(matrnrLayout, namenLayout, buttonLayout);
        add(content);
    }
}