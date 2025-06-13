package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

//Author: Tolga Cenk Kilic
public class BenutzerForm extends FormLayout {

    private final SecurityService securityService;
    private final BenutzerService benutzerService;

    Binder<Benutzer> binder = new BeanValidationBinder<>(Benutzer.class);
    Benutzer benutzer;
    String saltedPassword;

    TextField username = new TextField("Username");
    PasswordField password = new PasswordField("Password");
    TextField titel = new TextField("Titel");
    TextField vorname = new TextField("Vorname");
    TextField nachname = new TextField("Nachname");
    ComboBox<Benutzer.Role> role = new ComboBox<>("Role");

    Button saveButton = new Button("Speichern");
    Button deleteButton = new Button("Löschen");
    Button cancelButton = new Button("Abbrechen");

    public BenutzerForm(BenutzerService benutzerService, SecurityService securityService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;

        addClassName("benutzer-form");
        binder.bindInstanceFields(this);

        role.setItems(Benutzer.Role.values());

        add(
                username,
                password,
                titel,
                vorname,
                nachname,
                role,
                createButtonLayout()
        );
    }

    public void setBenutzer(Benutzer benutzer) {
        this.benutzer = benutzer;
        binder.readBean(benutzer);
        if(benutzer != null) {
            saltedPassword = benutzer.getPassword();
        }
        else {
            saltedPassword = "";
        }
        password.setValue("");
        if (!vorname.getValue().isBlank()) {
            saveButton.setText("Aktualisieren");
        }
        else {
            saveButton.setText("Speichern");
        }
    }

    private Component createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickListener(e -> validateAndSave());
        deleteButton.addClickListener(e -> fireEvent(new DeleteEvent(this, benutzer)));
        cancelButton.addClickListener(e -> fireEvent(new CloseEvent(this, benutzer)));

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(saveButton, deleteButton, cancelButton);
    }

    private void validateAndSave() {
        try {
            boolean isValid = true;


            if (username.getValue().isBlank()) {
                username.setInvalid(true);
                username.setErrorMessage("Benutzername darf nicht leer sein.");
                isValid = false;
            } else {
                username.setInvalid(false);
            }


            if (password.getValue().isBlank() && benutzer.getPassword() == null) {
                password.setInvalid(true);
                password.setErrorMessage("Passwort darf nicht leer sein.");
                isValid = false;
            } else {
                password.setInvalid(false);
            }

            String vornameVal = vorname.getValue().trim();
            if (vornameVal.isEmpty()) {
                vorname.setInvalid(true);
                vorname.setErrorMessage("Vorname darf nicht leer sein.");
                isValid = false;
            } else if (!vornameVal.matches("^[A-Za-zÄÖÜäöüß]+$")) {
                vorname.setInvalid(true);
                vorname.setErrorMessage("Vorname darf nur Buchstaben enthalten.");
                isValid = false;
            } else {
                vorname.setInvalid(false);
            }


            String nachnameVal = nachname.getValue().trim();
            if (nachnameVal.isEmpty()) {
                nachname.setInvalid(true);
                nachname.setErrorMessage("Nachname darf nicht leer sein.");
                isValid = false;
            } else if (!nachnameVal.matches("^[A-Za-zÄÖÜäöüß]+$")) {
                nachname.setInvalid(true);
                nachname.setErrorMessage("Nachname darf nur Buchstaben enthalten.");
                isValid = false;
            } else {
                nachname.setInvalid(false);
            }


            if (role.getValue() == null) {
                role.setInvalid(true);
                role.setErrorMessage("Bitte eine Rolle auswählen.");
                isValid = false;
            } else {
                role.setInvalid(false);
            }

            if (!isValid) {
                return; // Abbruch bei ungültiger Eingabe
            }

            binder.writeBean(benutzer);

            if (password.getValue().isBlank()) {
                benutzer.setPassword(saltedPassword);
            } else {
                benutzer.setPassword(securityService.encodePassword(password.getValue()));
            }

            if (benutzer.getAvatar() == null) {
                benutzer = benutzerService.createBenutzer(
                        benutzer.getUsername(),
                        benutzer.getVorname(),
                        benutzer.getNachname(),
                        benutzer.getPassword(),
                        benutzer.getRole()
                );
            }

            fireEvent(new SaveEvent(this, benutzer));

        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    //Events

    public static abstract class BenutzerFormEvent extends ComponentEvent<BenutzerForm> {
        private final Benutzer benutzer;

        protected BenutzerFormEvent(BenutzerForm source, Benutzer benutzer) {
            super(source, false);
            this.benutzer = benutzer;
        }

        public Benutzer getBenutzer() {
            return benutzer;
        }
    }


    public static class SaveEvent extends BenutzerFormEvent {
        SaveEvent(BenutzerForm source, Benutzer benutzer) {
            super(source, benutzer);
        }
    }

    public static class DeleteEvent extends BenutzerFormEvent {
        DeleteEvent(BenutzerForm source, Benutzer benutzer) {
            super(source, benutzer);
        }
    }

    public static class CloseEvent extends BenutzerFormEvent {
        CloseEvent(BenutzerForm source, Benutzer benutzer) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }

}
