package com.softwareprojekt.teambuilder.views;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

//Author: Silas Weber , Tolga Cenk Kilic
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profileinstellung | GroupForge")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final SecurityService securityService;

    private final Benutzer benutzer;
    private byte[] avatarBytes;

    //Komponenten
    private Avatar avatar;
    private Button savePasswordButton;
    private Button saveUsernameButton;
    private TextField usernameField;
    private TextField vornameField;
    private TextField nachnameField;
    private TextField titelField;
    private ComboBox<Benutzer.Role> userRole;
    private PasswordField newPasswordField;

    private PasswordField confirmPasswordField;

    private Button saveVornameButton;
    private Button saveNachnameButton;
    private Button saveTitelButton;



    public ProfileView(BenutzerService benutzerService,
                       SecurityService securityService) {

        this.securityService = securityService;
        this.benutzerService = benutzerService;

        String userDetails = securityService.getAuthenticatedUser().getUsername();
        this.benutzer = benutzerService.findBenutzerByUsername(userDetails);
        this.avatar = benutzerService.getAvatarByBenutzerId(benutzer.getId());
        initLayout();
    }



    private void initLayout() {

        add(new H1("Profil"));

        Div divider = new Div();
        divider.getStyle()
                .set("height", "1px")
                .set("width", "100%")
                .set("background-color", "#ccc") // graue Linie
                .set("margin-bottom", "20px");



        add(divider);

        vornameField = new TextField("Vorname");
        vornameField.setValue(benutzer.getVorname() != null ? benutzer.getVorname() : "");
        vornameField.setRequired(true);
        vornameField.setErrorMessage("Vorname darf nicht leer sein und nur Buchstaben enthalten.");
        saveVornameButton = new Button(VaadinIcon.CHECK.create());
        saveVornameButton.setVisible(false);
        saveVornameButton.addClickListener(e -> {
            if (validateVorname()) saveVorname(e);
        });
        HorizontalLayout vornameLayout = new HorizontalLayout(vornameField, saveVornameButton);
        vornameLayout.setAlignItems(Alignment.BASELINE);


        vornameField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (!validateVorname()) {
                saveVornameButton.setVisible(false);
                return;
            }
            saveVornameButton.setVisible(shouldShowSaveButton(
                    benutzer.getVorname(), value));
        });

        titelField= new TextField("Titel");
        titelField.setValue(benutzer.getTitel() != null ? benutzer.getTitel() : "");
        saveTitelButton = new Button(VaadinIcon.CHECK.create());

        saveTitelButton.setVisible(false);
        saveTitelButton.addClickListener(e -> saveTitel(e));
        HorizontalLayout titelLayout = new HorizontalLayout(titelField, saveTitelButton);
        titelLayout.setAlignItems(Alignment.BASELINE);

        titelField.addValueChangeListener(e -> {
            String neuerWert = e.getValue();
            String alterWert = benutzer.getTitel() != null ? benutzer.getTitel() : "";
            saveTitelButton.setVisible(!neuerWert.equals(alterWert));
        });


        nachnameField = new TextField("Nachname");
        nachnameField.setValue(benutzer.getNachname() != null ? benutzer.getNachname() : "");
        nachnameField.setRequired(true);
        nachnameField.setErrorMessage("Nachname darf nicht leer sein und nur Buchstaben enthalten.");
        saveNachnameButton = new Button(VaadinIcon.CHECK.create());
        saveNachnameButton.setVisible(false);
        saveNachnameButton.addClickListener(e -> {
            if (validateNachname()) saveNachname(e);
        });
        HorizontalLayout nachnameLayout = new HorizontalLayout(nachnameField, saveNachnameButton);
        nachnameLayout.setAlignItems(Alignment.BASELINE);


        nachnameField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (!validateNachname()) {
                saveNachnameButton.setVisible(false);
                return;
            }
            saveNachnameButton.setVisible(shouldShowSaveButton(
                    benutzer.getNachname(), value));
        });


        usernameField = new TextField("Benutzername");
        usernameField.setValue(benutzer.getUsername());
        usernameField.setRequired(true);
        usernameField.setErrorMessage("Benutzername darf nicht leer sein.");
        saveUsernameButton = new Button(VaadinIcon.CHECK.create());
        saveUsernameButton.setVisible(false);
        saveUsernameButton.addClickListener(e -> {
            if (validateUsername()) saveUsername(e);
        });


        usernameField = new TextField("Benutzername");
        usernameField.setValue(benutzer.getUsername());
        usernameField.setRequired(true);
        usernameField.setErrorMessage("Benutzername darf nicht leer sein.");
        saveUsernameButton = new Button(VaadinIcon.CHECK.create());
        saveUsernameButton.setVisible(false);
        saveUsernameButton.addClickListener(e -> {
            if (validateUsername()) saveUsername(e);
        });
        HorizontalLayout usernameLayout = new HorizontalLayout(usernameField, saveUsernameButton);
        usernameLayout.setAlignItems(Alignment.BASELINE);


        usernameField.addValueChangeListener(e -> {
            validateUsername();
        });

        userRole = new ComboBox<>("Rolle ");
        userRole.setItems(Benutzer.Role.values());
        userRole.setValue(benutzer.getRole());
        userRole.setReadOnly(true);

        newPasswordField = new PasswordField("Neues Passwort");
        confirmPasswordField = new PasswordField("Passwort wiederholen");


        savePasswordButton = new Button("Passwortänderung\nspeichern", VaadinIcon.CHECK.create());
        savePasswordButton.addClickListener(e -> savePassword(e));

        titelField.setWidth("200px");
        vornameField.setWidth("300px");
        nachnameField.setWidth("300px");
        usernameField.setWidth("300px");
        userRole.setWidth("300px");
        newPasswordField.setWidth("300px");
        confirmPasswordField.setWidth("300px");

        VerticalLayout fieldLayout = new VerticalLayout( );
        fieldLayout.setSpacing(true);
        fieldLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        fieldLayout.setAlignItems(Alignment.BASELINE);
        fieldLayout.setWidth("650px");


        H2 meinedaten = new H2("Meine Daten");

        HorizontalLayout nameLayout = new HorizontalLayout(vornameLayout, nachnameLayout);
        nameLayout.setWrap(true);


        H2 authentikation = new H2("Authentifizierung");
        authentikation.getStyle().set("margin-top", "25px");

        HorizontalLayout userLayout = new HorizontalLayout(usernameLayout, userRole);
        userLayout.setWrap(true);

        HorizontalLayout passwordLayout = new HorizontalLayout(newPasswordField, confirmPasswordField);
        passwordLayout.setWrap(true);

        Div passwortDivider = new Div();
        passwortDivider.setWidthFull();
        passwortDivider.setHeight("1px");

        passwortDivider.getStyle().set("background-color", "#ccc");

        H2 appearance = new H2("Aussehen");
        appearance.getStyle().set("margin-top", "25px");

        RadioButtonGroup<String> appearanceOptions = new RadioButtonGroup<>();
        appearanceOptions.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        appearanceOptions.setLabel("Farb Modus");
        appearanceOptions.setItems(Arrays.stream(Benutzer.Appearance.values()).map(Benutzer.Appearance::toString).toArray(String[]::new));
        appearanceOptions.setValue(benutzer.getAppearance());
        appearanceOptions.addValueChangeListener(e -> {
            benutzer.setAppearance(e.getValue());
            benutzerService.saveBenutzer(benutzer);

            ThemeList themeList = UI.getCurrent().getElement().getThemeList();

            if (e.getValue().equals(Benutzer.Appearance.Hell.toString())) {
                themeList.remove(Lumo.DARK);
            } else {
                themeList.add(Lumo.DARK);
            }
        });

        fieldLayout.add(
                meinedaten,
                titelLayout,
                nameLayout
        );

        fieldLayout.add(
                authentikation,
                userLayout,
                passwortDivider,
                passwordLayout,
                savePasswordButton
        );

        fieldLayout.add(
                appearance,
                appearanceOptions
                );


        Component avatarLayout = getAvatar();
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.addToStart(avatarLayout);
        mainLayout.addToMiddle(fieldLayout);

        add(mainLayout);
    }

    private void deleteAvatar(ClickEvent<Button> e) {
        Avatar avatarTmp = new Avatar(benutzer.getVorname() +" " + benutzer.getNachname());
        benutzer.setAvatar(avatarTmp);
        benutzer.setProfilePicture(null);
        benutzerService.saveBenutzer(benutzer);
        avatar = avatarTmp;
        Notification.show("Avatar wurde erfolgreich entfernt");
        UI.getCurrent().getPage().reload();
    }

    private void saveAvatar(ClickEvent<Button> e, byte[] avatarBytes) {
        benutzer.setProfilePicture(avatarBytes);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Avatar wurde erfolgreich gespeichert");
        avatar = benutzerService.getAvatarByBenutzerId(benutzer.getId());
        UI.getCurrent().getPage().reload();
    }

    private void saveVorname(ClickEvent<Button> e) {
        String vorname = vornameField.getValue();
        benutzer.setVorname(vorname);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Vorname aktualisiert");
        //Nach dem Speichern Button ausblenden
        saveVornameButton.setVisible(false);
    }

    private void saveNachname(ClickEvent<Button> e) {
        String nachname = nachnameField.getValue();
        benutzer.setNachname(nachname);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Nachname aktualisiert");
        //Nach dem Speichern Button ausblenden
        saveNachnameButton.setVisible(false);
    }

    private void saveUsername(ClickEvent<Button> e) {
        String username= usernameField.getValue();
        benutzer.setUsername(username);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Benutzername aktualisiert");
        saveUsernameButton.setVisible(false);
    }

    private void saveTitel(ClickEvent<Button> e) {
        String titel= titelField.getValue();
        benutzer.setTitel(titel);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Titel aktualisiert");
        saveTitelButton.setVisible(false);
    }

    private boolean validateVorname() {
        String vorname = vornameField.getValue();
        if (vorname.trim().isEmpty()) {
            vornameField.setInvalid(true);
            vornameField.setErrorMessage("Vorname darf nicht leer sein.");
            return false;
        }
        if (!vorname.matches("^[a-zA-ZäöüÄÖÜß\\-\\s]+$")) {
            vornameField.setInvalid(true);
            vornameField.setErrorMessage("Vorname darf nur Buchstaben enthalten.");
            return false;
        }
        vornameField.setInvalid(false);
        return true;
    }

    private boolean validateNachname() {
        String nachname = nachnameField.getValue();
        if (nachname.trim().isEmpty()) {
            nachnameField.setInvalid(true);
            nachnameField.setErrorMessage("Nachname darf nicht leer sein.");
            return false;
        }
        if (!nachname.matches("^[a-zA-ZäöüÄÖÜß\\-\\s]+$")) {
            nachnameField.setInvalid(true);
            nachnameField.setErrorMessage("Nachname darf nur Buchstaben enthalten.");
            return false;
        }
        nachnameField.setInvalid(false);
        return true;
    }

    private boolean validateUsername() {
        String neuerBenutzername = usernameField.getValue();
        String aktuellerBenutzername = benutzer.getUsername();

        if (neuerBenutzername.trim().isEmpty()) {
            usernameField.setInvalid(true);
            usernameField.setErrorMessage("Benutzername darf nicht leer sein.");
            saveUsernameButton.setVisible(false);
            return false;
        }

        if (neuerBenutzername.equals(aktuellerBenutzername)) {
            usernameField.setInvalid(false);
            usernameField.setErrorMessage(null);
            saveUsernameButton.setVisible(false);
            return true;
        }

        Benutzer vorhandenerBenutzer = benutzerService.findBenutzerByUsername(neuerBenutzername);
        if (vorhandenerBenutzer != null) {
            usernameField.setInvalid(true);
            usernameField.setErrorMessage("Benutzername ist bereits vergeben.");
            saveUsernameButton.setVisible(false);
            return false;
        } else {
            usernameField.setInvalid(false);
            usernameField.setErrorMessage(null);
            saveUsernameButton.setVisible(true);
            return true;
        }
    }

    private boolean shouldShowSaveButton(String alt, String neu) {
        alt = alt != null ? alt : "";
        neu = neu != null ? neu : "";
        return !alt.equals(neu);
    }

    private void savePassword(ClickEvent<Button> e) {

        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();


        if(newPassword.isEmpty() || confirmPassword.isEmpty()){
            Notification.show("Bitte geben Sie ein neues Passwort ein.");
            return;
        }

        //Beide Passwortfelder müssen übereinstimmen
        if(!newPassword.equals(confirmPassword))
        {
         Notification.show("Passwörter stimmen nicht überein.");
         return;
        }

        //Passwort vor dem Speichern wieder verschlüsseln
        String passwortEncrypted = securityService.encodePassword(newPassword);
        benutzer.setPassword(passwortEncrypted);
        benutzerService.saveBenutzer(benutzer);
        Notification.show("Passwort aktualisiert");

        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    public Component getAvatar() {

        //AvatarLayout mit Buttons
        VerticalLayout avatarLayout = new VerticalLayout();
        avatarLayout.setWidth("25%");
        avatarLayout.setHeightFull();
        avatarLayout.setAlignItems(Alignment.CENTER);
        avatarLayout.setMinWidth("300px");
        avatarLayout.setMinHeight("650px");


        avatar.setHeight("300px");
        avatar.setWidth("300px");

        H4 title = new H4("Profilbild hochladen");

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setAutoUpload(true);
        upload.setMaxFiles(1);

        int maxFileSizeInBytes = 10 * 1024 * 1024; // 10MB
        upload.setMaxFileSize(maxFileSizeInBytes);

        upload.setAcceptedFileTypes("image/jpeg", "image/png");

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

         upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);

            try {
                avatarBytes = inputStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

             StreamResource avatarResource = new StreamResource("image", new InputStreamFactory() {
                 @Override
                 public InputStream createInputStream() {
                     return new ByteArrayInputStream(avatarBytes);
                 }
             });

            avatar.setImageResource(avatarResource);

        });

        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button saveButton = new Button("Speichern", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveAvatar(e, avatarBytes));

        Button deleteButton = new Button("Löschen", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addClickListener(this::deleteAvatar);

        buttonLayout.add(deleteButton, saveButton);


        avatarLayout.add(avatar, title, upload, buttonLayout);

        return avatarLayout;
    }


}
