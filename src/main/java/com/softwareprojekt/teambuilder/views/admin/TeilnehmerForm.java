package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
//Author: Silas Weber
public class TeilnehmerForm extends FormLayout {

    Binder<Teilnehmer> binder = new BeanValidationBinder<>(Teilnehmer.class);
    Teilnehmer teilnehmer;

    TextField matrnr = new TextField("Matrikelnummer");
    TextField vorname = new TextField("Vorname");
    TextField nachname = new TextField("Nachname");

    Button saveButton = new Button("Speichern");
    Button deleteButton = new Button("Löschen");
    Button cancelButton = new Button("Abbrechen");

    public TeilnehmerForm() {

        binder.bindInstanceFields(this);

        matrnr.setPattern("[0-9]{8}");
        matrnr.setMaxLength(8);
        matrnr.setMinLength(8);
        matrnr.setReadOnly(true);
        vorname.setRequired(true);
        nachname.setRequired(true);


        // ValueChangeListener für Vorname
        vorname.addValueChangeListener(e -> {
            if(e.getValue() == null || e.getValue().trim().isEmpty()) {
                vorname.setInvalid(true);
                vorname.setErrorMessage("Bitte geben Sie einen Vornamen ein.");
                saveButton.setEnabled(false);
            } else {
                vorname.setInvalid(false);
            }
        });

        // ValueChangeListener für Nachname
        nachname.addValueChangeListener(e -> {
            if(e.getValue() == null || e.getValue().trim().isEmpty()) {
                nachname.setInvalid(true);
                nachname.setErrorMessage("Bitte geben Sie einen Nachnamen ein.");
                saveButton.setEnabled(false);
            } else {
                nachname.setInvalid(false);
            }
        });

                binder.forField(matrnr)
                        .withConverter(
                                str -> str.isEmpty() ? null : Long.parseLong(str.replaceAll("[.,]", "")),
                                num -> num == null ? "" : String.format("%d", num),
                                "Bitte geben Sie eine gültige Matrikelnummer ein"
                        )
                        .bind(Teilnehmer::getMatrnr, Teilnehmer::setMatrnr);

        add(
                matrnr,
                vorname,
                nachname,
                createButtonLayout()
        );
    }

    public void setTeilnehmer(Teilnehmer teilnehmer) {
        this.teilnehmer = teilnehmer;
        binder.readBean(teilnehmer);

        if(teilnehmer != null && teilnehmer.getMatrnr() == 1L) {
            matrnr.setValue("");
        }

    }

    private Component createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickListener(e -> validateAndSave());
        deleteButton.addClickListener(e -> fireEvent(new DeleteEvent(this, teilnehmer)));
        cancelButton.addClickListener(e -> fireEvent(new CloseEvent(this, teilnehmer)));

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(saveButton, deleteButton, cancelButton);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(teilnehmer);

            fireEvent(new TeilnehmerForm.SaveEvent(this, teilnehmer));
        }
        catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    //Events

    public static abstract class TeilnehmerFormEvent extends ComponentEvent<TeilnehmerForm> {
        private final Teilnehmer teilnehmer;

        protected TeilnehmerFormEvent(TeilnehmerForm source, Teilnehmer teilnehmer) {
            super(source, false);
            this.teilnehmer = teilnehmer;
        }

        public Teilnehmer getTeilnehmer() {
            return teilnehmer;
        }
    }


    public static class SaveEvent extends TeilnehmerFormEvent {
        SaveEvent(TeilnehmerForm source, Teilnehmer teilnehmer) {
            super(source, teilnehmer);
        }
    }

    public static class DeleteEvent extends TeilnehmerFormEvent {
        DeleteEvent(TeilnehmerForm source, Teilnehmer teilnehmer) {
            super(source, teilnehmer);
        }
    }

    public static class CloseEvent extends TeilnehmerFormEvent {
        CloseEvent(TeilnehmerForm source, Teilnehmer teilnehmer) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }

}
