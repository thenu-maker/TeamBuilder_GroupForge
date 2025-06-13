package com.softwareprojekt.teambuilder.views.dialog;

import com.softwareprojekt.teambuilder.entities.Termin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;
//Author: Tolga Cenk Kilic
public abstract class AbstractTerminDialog extends Dialog {
    protected DatePicker datePicker;
    protected TimePicker startZeit;
    protected TimePicker endZeit;
    protected Button saveButton;
    protected Button cancelButton;
    protected Consumer<Termin> saveCallback;

    protected void setupCommonLayout(String titleText) {
        setModal(true);
        setDraggable(true);
        setResizable(true);

        H1 title = new H1(titleText);
        Div divider = createDivider();
        add(title, divider);

        initializeFields();
        setupFieldValidation();

        VerticalLayout fieldLayout = new VerticalLayout(datePicker, startZeit, endZeit);
        fieldLayout.setPadding(true);
        fieldLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setPadding(true);
        buttonLayout.setSpacing(true);

        add(fieldLayout, buttonLayout);
        setWidth("700px");
        setHeight("auto");
    }

    private Div createDivider() {
        Div divider = new Div();
        divider.getStyle()
                .set("height", "1px")
                .set("width", "100%")
                .set("background-color", "#ccc")
                .set("margin-bottom", "20px");
        return divider;
    }

    private void initializeFields() {
        datePicker = new DatePicker("Datum");
        startZeit = new TimePicker("Startzeit");
        startZeit.setStep(Duration.ofMinutes(15));
        endZeit = new TimePicker("Endzeit");
        endZeit.setStep(Duration.ofMinutes(15));

        saveButton = new Button(getSaveButtonText(), VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    }

    protected abstract String getSaveButtonText();

    private void setupFieldValidation() {
        datePicker.setRequired(true);
        startZeit.setRequired(true);
        endZeit.setRequired(true);
    }

    protected boolean validateFields() {
        boolean isValid = true;

        if (datePicker.getValue() == null) {
            datePicker.setInvalid(true);
            datePicker.setErrorMessage("Bitte Datum angeben");
            isValid = false;
        } else {
            datePicker.setInvalid(false);
        }

        if (startZeit.getValue() == null) {
            startZeit.setInvalid(true);
            startZeit.setErrorMessage("Bitte Startzeit angeben");
            isValid = false;
        } else {
            startZeit.setInvalid(false);
        }

        if (endZeit.getValue() == null) {
            endZeit.setInvalid(true);
            endZeit.setErrorMessage("Bitte Endzeit angeben");
            isValid = false;
        } else {
            endZeit.setInvalid(false);
        }

        LocalTime start = startZeit.getValue();
        LocalTime end = endZeit.getValue();

        if (start != null && end != null && (start.isAfter(end) || start.equals(end))) {
            startZeit.setErrorMessage("Startzeit muss vor Endzeit liegen");
            startZeit.setInvalid(true);
            isValid = false;
        } else if (start != null && end != null) {
            startZeit.setInvalid(false);
        }

        return isValid;
    }

    public void setSaveCallback(Consumer<Termin> saveCallback) {
        this.saveCallback = saveCallback;
    }
}