package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.services.TerminService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;

//Author: Tolga Cenk Kilic
public class TermineBearbeitenDialog extends AbstractTerminDialog {
    private final TerminService terminService;
    private final Termin termin;
    private Button removeButton;
    private Consumer<Termin> deleteCallback;

    public TermineBearbeitenDialog(TerminService terminService,
                                   Termin termin) {
        this.terminService = terminService;
        this.termin = termin;
        layoutAusfuehren();
    }

    private void layoutAusfuehren() {
        setupCommonLayout("Termin Bearbeiten");


        datePicker.setValue(termin.getDatum());
        startZeit.setValue(termin.getStartzeit());
        endZeit.setValue(termin.getEndzeit());

        // Löschen-Button hinzufügen
        removeButton = new Button("Löschen", VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        removeButton.addClickListener(e -> confirmDelete());

        // Button-Layout anpassen
        HorizontalLayout buttonLayout = (HorizontalLayout) getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .findFirst().orElseThrow();
        buttonLayout.add(removeButton);

        saveButton.addClickListener(e -> speichern());
        cancelButton.addClickListener(e -> close());
    }

    @Override
    protected String getSaveButtonText() {
        return "Speichern";
    }

    private void speichern() {
        if (!validateFields()) {
            return;
        }

        termin.setDatum(datePicker.getValue());
        termin.setStartzeit(startZeit.getValue());
        termin.setEndzeit(endZeit.getValue());

        terminService.save(termin);

        if (saveCallback != null) {
            saveCallback.accept(termin);
        }

        close();
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog(
                "Termin löschen",
                "Sind Sie sicher, dass Sie diesen Termin löschen möchten?",
                "Löschen", e -> deleteTermin(),
                "Abbrechen", e -> {});
        dialog.open();
    }

    private void deleteTermin() {
        terminService.delete(termin);
        if (deleteCallback != null) {
            deleteCallback.accept(termin);
        }
        close();
    }

}
