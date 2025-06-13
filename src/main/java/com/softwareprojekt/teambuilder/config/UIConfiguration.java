package com.softwareprojekt.teambuilder.config;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.stereotype.Component;
//Author: Silas Weber

@Component
public class UIConfiguration implements VaadinServiceInitListener {

    private final BenutzerService benutzerService;
    private final SecurityService securityService;

    public UIConfiguration(BenutzerService benutzerService, SecurityService securityService) {
        this.benutzerService = benutzerService;
        this.securityService = securityService;
    }

    @Override
    public void serviceInit(com.vaadin.flow.server.ServiceInitEvent event) {
        event.getSource().addUIInitListener(new UIInitListener() {
            @Override
            public void uiInit(com.vaadin.flow.server.UIInitEvent event) {
                UI ui = event.getUI();
                try {
                    String username = securityService.getAuthenticatedUser().getUsername();
                    Benutzer benutzer = benutzerService.findBenutzerByUsername(username);

                    if (benutzer != null && benutzer.getAppearance() != null) {
                        if (benutzer.getAppearance().equals(Benutzer.Appearance.Dunkel.toString())) {
                            ui.getElement().getThemeList().add(Lumo.DARK);
                        }
                    }
                } catch (Exception e) {
                    // Benutzer ist nicht eingeloggt oder ein anderer Fehler ist aufgetreten
                }
            }
        });
    }
}

