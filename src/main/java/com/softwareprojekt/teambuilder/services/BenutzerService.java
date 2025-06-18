package com.softwareprojekt.teambuilder.services;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.repository.BenutzerRepository;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
//Author: Silas Weber
@Service
public class BenutzerService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private BenutzerRepository benutzerRepository;
    @Autowired
    private VeranstaltungService veranstaltungService;
    @PersistenceContext
    private EntityManager entityManager;



    public Benutzer createBenutzer(String username, String vorname, String nachname, String password) {
        Avatar avatar = new Avatar(vorname + " " + nachname);
        Benutzer benutzer = new Benutzer(username, vorname, nachname, securityService.encodePassword(password), Benutzer.Role.PROFESSOR);
        return benutzer;
    }

    public Benutzer createBenutzer(String username, String vorname, String nachname, String password, Benutzer.Role role) {
        Avatar avatar = new Avatar(vorname + " " + nachname);
        Benutzer benutzer = new Benutzer(username, vorname, nachname, securityService.encodePassword(password),role,avatar);
        return benutzer;
    }

    public Benutzer createBenutzer(String username, String vorname, String nachname, String password, Benutzer.Role role, String imgAvatar) {
        Avatar avatar = new Avatar(vorname + " " + nachname, imgAvatar);
        Benutzer benutzer = new Benutzer(username, vorname, nachname, securityService.encodePassword(password),role,avatar);
        return benutzer;
    }

    public Benutzer createBenutzer(String username, String vorname, String nachname, String password, Benutzer.Role role, Avatar avatar) {
        Benutzer benutzer = new Benutzer(username, vorname, nachname, securityService.encodePassword(password),role, avatar);
        return benutzer;
    }

    public Benutzer findBenutzerByUsername(String username) {return benutzerRepository.findByUsername(username);}

    public Optional<Benutzer> findBenutzerByid(long id) {return benutzerRepository.findById(id);}

    public List<Benutzer> findAllBenutzers() {return benutzerRepository.findAll(); }

    public List<Benutzer> findAllBenutzer(String filterText) {
        if(filterText == null || filterText.isEmpty()) {
            return benutzerRepository.findAll();
        }
        else {
            return benutzerRepository.search(filterText);
        }
    }

    public long countBenutzers() {return benutzerRepository.count();}

    public void saveBenutzer(Benutzer benutzer){

        if (benutzer == null) {
            System.out.println("Benutzer is null");
            return;
        }

        benutzerRepository.save(benutzer);
    }

    @Transactional
    public void deleteBenutzer(Benutzer benutzer) {
        Benutzer managed = findBenutzerByid(benutzer.getId()).orElse(null);
        if (managed != null) {
            List<Veranstaltung> veranstaltungen = managed.getVeranstaltungen();
            if (veranstaltungen == null || veranstaltungen.isEmpty()) {
                System.out.println("Keine Veranstaltungen zum Löschen gefunden.");

            } else {
                for (Veranstaltung veranstaltung : veranstaltungen) {
                    veranstaltungService.deleteVeranstaltungMitAbhaengigkeiten(veranstaltung);
                    managed.removeVeranstaltung(veranstaltung);
                }
                entityManager.flush();
                saveBenutzer(managed);
            }
            benutzerRepository.delete(managed);
        } else {
            System.out.println("Benutzer nicht gefunden.");
        }
    }

    public Avatar getAvatarByBenutzerId(Long benutzerId) {

        Benutzer benutzer = benutzerRepository.findById(benutzerId).orElse(null);

        Avatar avatar = benutzer.getAvatar();

        if(benutzer.getProfilePicture() != null) {
            StreamResource avatarResource = new StreamResource("image", new InputStreamFactory() {
                @Override
                public InputStream createInputStream() {
                    return new ByteArrayInputStream(benutzer.getProfilePicture());
                }
            });
            avatar.setImageResource(avatarResource);
        }
        return avatar;
    }
}
