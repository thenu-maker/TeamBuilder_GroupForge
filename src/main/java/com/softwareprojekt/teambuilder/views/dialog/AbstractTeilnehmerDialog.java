package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;
//Author: Tolga Cenk Kilic
public abstract class AbstractTeilnehmerDialog extends Dialog {
    protected static final String DIALOG_WIDTH = "700px";
    protected static final String NAME_REGEX = "[\\p{L}\\- ]+";
    protected static final String MATRNR_REGEX = "[0-9]{8}";
    protected final TeilnehmerService teilnehmerService;
    protected Consumer<Teilnehmer> saveCallback;
    protected TextField matrnrField;
    protected TextField vornameField;
    protected TextField nachnameField;

    public AbstractTeilnehmerDialog(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    protected void initCommonLayout(String titleText) {
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth(DIALOG_WIDTH);
        setHeight("auto");

        H1 title = new H1(titleText);
        Div divider = new Div();
        divider.getStyle()
                .set("height", "1px")
                .set("width", "100%")
                .set("background-color", "#ccc")
                .set("margin-bottom", "20px");
        add(title, divider);
    }

    protected void aehnlicheFelderKonfigurieren(Teilnehmer teilnehmer) {
        matrnrField = new TextField("Matrikelnummer");
        vornameField = new TextField("Vorname");
        nachnameField = new TextField("Nachname");

        String fieldWidth = "100%";
        vornameField.setWidth(fieldWidth);
        nachnameField.setWidth(fieldWidth);

        matrnrField.getStyle().set("--vaadin-text-field-default-width", "100%");
        matrnrField.getStyle().set("padding-left", "0");
        vornameField.getStyle().set("padding-left", "0");
        nachnameField.getStyle().set("padding-left", "0");

        matrnrField.setRequired(true);
        vornameField.setRequired(true);
        nachnameField.setRequired(true);

        matrnrField.setPattern(MATRNR_REGEX);
        matrnrField.setMaxLength(8);
        matrnrField.setMinLength(8);

        // ValueChangeListener für Matrikelnummer
        matrnrField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value == null || value.isEmpty()) {
                matrnrField.setInvalid(true);
                matrnrField.setErrorMessage("Matrikelnummer muss angegeben werden");
            } else if (!value.matches(MATRNR_REGEX)) {
                matrnrField.setInvalid(true);
                matrnrField.setErrorMessage("Die Matrikelnummer muss genau 8 Ziffern enthalten.");
            } else {
                matrnrField.setInvalid(false);
            }
        });

        // ValueChangeListener für Vorname
        vornameField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value == null || value.isEmpty()) {
                vornameField.setInvalid(true);
                vornameField.setErrorMessage("Vorname muss angegeben werden");
            } else if (!value.matches(NAME_REGEX)) {
                vornameField.setInvalid(true);
                vornameField.setErrorMessage("Vorname darf nur Buchstaben enthalten");
            } else {
                vornameField.setInvalid(false);
            }
        });

        // ValueChangeListener für Nachname
        nachnameField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value == null || value.isEmpty()) {
                nachnameField.setInvalid(true);
                nachnameField.setErrorMessage("Nachname muss angegeben werden");
            } else if (!value.matches(NAME_REGEX)) {
                nachnameField.setInvalid(true);
                nachnameField.setErrorMessage("Nachname darf nur Buchstaben enthalten");
            } else {
                nachnameField.setInvalid(false);
            }
        });

        if (teilnehmer != null) {
            matrnrField.setValue(String.valueOf(teilnehmer.getMatrnr()));
            vornameField.setValue(teilnehmer.getVorname());
            nachnameField.setValue(teilnehmer.getNachname());
        }
    }

    protected boolean felderUeberpruefung() {
        boolean valid = true;

        // Validate Matrikelnummer
        String matrnrValue = matrnrField.getValue();
        if (matrnrValue == null || matrnrValue.isEmpty()) {
            matrnrField.setInvalid(true);
            matrnrField.setErrorMessage("Matrikelnummer muss angegeben werden");
            valid = false;
        } else if (!matrnrValue.matches(MATRNR_REGEX)) {
            matrnrField.setInvalid(true);
            matrnrField.setErrorMessage("Die Matrikelnummer muss genau 8 Ziffern enthalten.");
            valid = false;
        } else {
            matrnrField.setInvalid(false);
        }

        // Validate Vorname
        String vorname = vornameField.getValue();
        if (vorname == null || vorname.isEmpty()) {
            vornameField.setInvalid(true);
            vornameField.setErrorMessage("Vorname muss angegeben werden");
            valid = false;
        } else if (!vorname.matches(NAME_REGEX)) {
            vornameField.setInvalid(true);
            vornameField.setErrorMessage("Vorname darf nur Buchstaben enthalten");
            valid = false;
        } else {
            vornameField.setInvalid(false);
        }

        // Validate Nachname
        String nachname = nachnameField.getValue();
        if (nachname == null || nachname.isEmpty()) {
            nachnameField.setInvalid(true);
            nachnameField.setErrorMessage("Nachname muss angegeben werden");
            valid = false;
        } else if (!nachname.matches(NAME_REGEX)) {
            nachnameField.setInvalid(true);
            nachnameField.setErrorMessage("Nachname darf nur Buchstaben enthalten");
            valid = false;
        } else {
            nachnameField.setInvalid(false);
        }

        return valid;
    }

    public void setSaveCallback(Consumer<Teilnehmer> saveCallback) {
        this.saveCallback = saveCallback;
    }
}