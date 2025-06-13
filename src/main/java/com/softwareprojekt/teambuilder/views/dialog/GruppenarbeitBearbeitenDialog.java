package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.entities.Termin;
import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;
//Author: Tolga Cenk Kilic
public class GruppenarbeitBearbeitenDialog extends Dialog {

    private final GruppenarbeitService gruppenarbeitService;
    private Gruppenarbeit gruppenarbeit;
    private Consumer<Gruppenarbeit> saveCallback;

    TextField name = new TextField("Gruppenarbeit");
    Button saveButton = new Button("Speichern");
    Button cancelButton = new Button("Abbrechen");


    public GruppenarbeitBearbeitenDialog(GruppenarbeitService gruppenarbeitService,
                                         Gruppenarbeit gruppenarbeit) {
        this.gruppenarbeitService = gruppenarbeitService;
        this.gruppenarbeit = gruppenarbeit;

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            gruppenarbeit.setTitel(name.getValue());
            gruppenarbeitService.save(gruppenarbeit);
            if (saveCallback != null) saveCallback.accept(gruppenarbeit);
            close();
        });

        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.addClickListener(e -> close());

        VerticalLayout content = new VerticalLayout();

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        content.add(name, buttonLayout);
        add(content);

        name.setValue(gruppenarbeit.getTitel());
    }

    public void setSaveCallback(Consumer<Gruppenarbeit> saveCallback) {
        this.saveCallback = saveCallback;
    }
}
