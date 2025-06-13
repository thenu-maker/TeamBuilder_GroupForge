package com.softwareprojekt.teambuilder;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.vaadin.flow.component.avatar.Avatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AdminCreator implements CommandLineRunner {

    private final BenutzerService benutzerService;
    private final SecurityService securityService;


    @Value("${system.adminuser.username}")
    private  String ADMIN_USERNAME;

    @Value("${system.adminuser.passwort}")
    private  String ADMIN_PASSWORD;

    @Value("${system.adminuser.vorname}")
    private String ADMIN_VORNAME;

    @Value("${system.adminuser.nachname}")
    private String ADMIN_NACHNAME;

    public AdminCreator(BenutzerService benutzerService, SecurityService securityService) {
        this.benutzerService = benutzerService;
        this.securityService = securityService;

    }

    @Override
    public void run(String... args) throws Exception {
        if (benutzerService.countBenutzers() == 0) {
            Benutzer admin = new Benutzer();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword(securityService.encodePassword(ADMIN_PASSWORD));
            admin.setRole(Benutzer.Role.ADMIN);
            admin.setVorname(ADMIN_VORNAME);
            admin.setNachname(ADMIN_NACHNAME);
            admin.setAvatar(new Avatar(admin.getVorname() +" "+ admin.getNachname()));
            admin.setAppearance(Benutzer.Appearance.Hell.toString());
            benutzerService.saveBenutzer(admin);
        }
        ADMIN_USERNAME = null;
        ADMIN_PASSWORD = null;

    }
}
