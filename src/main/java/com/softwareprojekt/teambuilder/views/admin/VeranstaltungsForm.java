package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.SemesterService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.util.List;
//Author: Silas Weber
public class VeranstaltungsForm extends FormLayout {

    private final BenutzerService benutzerService;
    private final SemesterService semesterService;

    Binder<Veranstaltung> binder = new BeanValidationBinder<>(Veranstaltung.class);
    Veranstaltung veranstaltung;


    TextField titel = new TextField("Titel");
    ComboBox<String> semester = new ComboBox<>("Semester");
    ComboBox<Benutzer> benutzer = new ComboBox<>("Benutzer");

    Button saveButton = new Button("Speichern");
    Button deleteButton = new Button("Löschen");
    Button cancelButton = new Button("Abbrechen");

    public VeranstaltungsForm(BenutzerService benutzerService,
                              SemesterService semesterService ) {
        this.benutzerService = benutzerService;
        this.semesterService = semesterService;

        binder.bindInstanceFields(this);

        semester.setItems(semesterService.getMomSemesters());

        benutzer.setItemLabelGenerator(Benutzer::getUsername);
        List<Benutzer> benutzerList = benutzerService.findAllBenutzers();
        benutzer.setItems(benutzerList);

        titel.setRequired(true);
        semester.setRequired(true);

        add(
                titel,
                semester,
                benutzer,
                createButtonLayout()
        );
    }

    public void setVeranstaltung(Veranstaltung veranstaltung) {
        this.veranstaltung = veranstaltung;
        binder.readBean(veranstaltung);
    }

    private Component createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickListener(e -> validateAndSave());
        deleteButton.addClickListener(e -> fireEvent(new DeleteEvent(this, veranstaltung)));
        cancelButton.addClickListener(e -> fireEvent(new CloseEvent(this, veranstaltung)));

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(saveButton, deleteButton, cancelButton);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(veranstaltung);

            fireEvent(new SaveEvent(this, veranstaltung));
        }
        catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    //Events

    public static abstract class VeranstaltungFormEvent extends ComponentEvent<VeranstaltungsForm> {
        private final Veranstaltung veranstaltung;

        protected VeranstaltungFormEvent(VeranstaltungsForm source, Veranstaltung veranstaltung) {
            super(source, false);
            this.veranstaltung = veranstaltung;
        }

        public Veranstaltung getVeranstaltung() {
            return veranstaltung;
        }
    }


    public static class SaveEvent extends VeranstaltungFormEvent {
        SaveEvent(VeranstaltungsForm source, Veranstaltung veranstaltung) {
            super(source, veranstaltung);
        }
    }

    public static class DeleteEvent extends VeranstaltungFormEvent {
        DeleteEvent(VeranstaltungsForm source, Veranstaltung veranstaltung) {
            super(source, veranstaltung);
        }
    }

    public static class CloseEvent extends VeranstaltungFormEvent {
        CloseEvent(VeranstaltungsForm source, Veranstaltung veranstaltung) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }

}
