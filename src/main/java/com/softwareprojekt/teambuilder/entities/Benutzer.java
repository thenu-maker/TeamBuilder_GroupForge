package com.softwareprojekt.teambuilder.entities;
import com.vaadin.flow.component.avatar.Avatar;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.List;

//Author: Silas Weber
@Entity
public class Benutzer {

    public boolean isEmpty() {
        return false;
    }

    public enum Role {
        ADMIN,
        PROFESSOR,
    }

    public enum Appearance {
        Hell,
        Dunkel;
    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @NotNull
    @Unique
    @NotBlank(message = "Bitte geben Sie einen Benutzernamen ein.")
    private String username;

    @NotNull
    private String password;

    @NotNull
    @Pattern(regexp = "^[A-Za-zÄäÖöÜüß\\-]+$",
            message = "Der Vorname darf nur Buchstaben enthalten")
    @NotBlank(message = "Bitte geben Sie einen Vorname ein.")
    private String vorname;

    @NotNull
    @Pattern(regexp = "^[A-Za-zÄäÖöÜüß\\-]+$",
            message = "Der Nachname darf nur Buchstaben enthalten")
    @NotBlank(message = "Bitte geben Sie einen Nachnamen ein.")
    private String nachname;

    private Avatar avatar;

    private String titel;

    @NotEmpty
    private String appearance;

    @NotNull
    private Role role;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "benutzer")
    private List<Veranstaltung> veranstaltungen;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] profilePicture;

    public Benutzer() {}

    public Benutzer(String username, String vorname, String nachname, String password, Role role) {
        this.username = username;
        this.vorname = vorname;
        this.nachname = nachname;
        this.password = password;
        this.role = role;
        this.appearance = Appearance.Hell.toString();
    }

    public Benutzer(String username, String vorname, String nachname, String password, Role role,Avatar avatar) {
        this.username = username;
        this.vorname = vorname;
        this.nachname = nachname;
        this.password = password;
        this.role = role;
        this.avatar = avatar;
        this.appearance = Appearance.Hell.toString();
    }

    public Benutzer(String titel, String username, String vorname, String nachname, String password, Role role,Avatar avatar) {
        this.username = username;
        this.vorname = vorname;
        this.nachname = nachname;
        this.password = password;
        this.role = role;
        this.avatar = avatar;
        this.titel = titel;
        this.appearance = Appearance.Hell.toString();
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVorname(String vorname){
        this.vorname = vorname;
    }

    public String getVorname(){
        return vorname;
    }

    public void setNachname(String nachname){
        this.nachname = nachname;
    }

    public String getNachname(){
        return nachname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void addVeranstaltung(Veranstaltung veranstaltung){
        veranstaltungen.add(veranstaltung);
    }

    public void removeVeranstaltung(Veranstaltung veranstaltung){
        veranstaltungen.remove(veranstaltung);
    }

    public List<Veranstaltung> getVeranstaltungen(){
        return veranstaltungen.stream().toList();
    }

    public void setVeranstaltungen(List<Veranstaltung> veranstaltungen) {
        this.veranstaltungen = veranstaltungen;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }
}