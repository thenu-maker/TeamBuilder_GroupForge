package com.softwareprojekt.teambuilder.views;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.views.uebersicht.DashboardView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;

//Author: Silas Weber
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private final BenutzerService benutzerService;

    private final H1 momPage = new H1();

    public MainLayout(SecurityService securityService,
                      BenutzerService benutzerService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;

        createHeader();
    }

    private void createHeader() {
        Image image = new Image("images/logo-gf.png", "GroupForge");
        image.setWidth("100px");
        image.setHeight("60px");
        image.getStyle().setMargin("10px");
        image.addClickListener(event -> {UI.getCurrent().navigate(DashboardView.class);});

        H1 momPageDiv = new H1();
        momPageDiv.addClassNames("text-l");
        momPageDiv.setText("|");

        momPage.addClassNames("text-l");

        HorizontalLayout header = new HorizontalLayout();

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("flex-wrap", "wrap");
        header.getStyle().set("gap", "1rem");
        header.setSpacing("spacing-s");
        header.setWidthFull();
        header.setWrap(true);

        header.add(image);
        header.add(momPageDiv);
        header.add(momPage);

        HorizontalLayout navLayout = new HorizontalLayout();
        navLayout.setWidth("60%");
        navLayout.setWrap(true);


        HorizontalLayout dashboardLayout = new HorizontalLayout();
        dashboardLayout.setPadding(true);
        dashboardLayout.addClickListener(e -> UI.getCurrent().navigate(DashboardView.class));

        H1 dasboard = new H1("Dashboard");
        dasboard.addClassNames("text-l");

        Icon dashboardIcon = VaadinIcon.DASHBOARD.create();
        dashboardIcon.setSize("20px");

        dashboardLayout.add(dashboardIcon, dasboard);


        HorizontalLayout veranstaltungenLayout = new HorizontalLayout();
        veranstaltungenLayout.setPadding(true);
        veranstaltungenLayout.addClickListener(e -> UI.getCurrent().navigate("veranstaltungen"));

        H1 veranstaltungen = new H1("Veranstaltungen");
        veranstaltungen.addClassNames("text-l");

        Icon veranstaltungIcon = VaadinIcon.CALENDAR_O.create();
        veranstaltungIcon.setSize("20px");

        veranstaltungenLayout.add(veranstaltungIcon, veranstaltungen);


        HorizontalLayout teilnehmerLayout = new HorizontalLayout();
        teilnehmerLayout.setPadding(true);
        teilnehmerLayout.addClickListener(e -> UI.getCurrent().navigate("teilnehmer"));

        H1 teilnehmer = new H1("Teilnehmer");
        teilnehmer.addClassNames("text-l");

        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("20px");

        teilnehmerLayout.add(userIcon, teilnehmer);


        HorizontalLayout gruppenLayout = new HorizontalLayout();
        gruppenLayout.setPadding(true);
        gruppenLayout.addClickListener(e -> UI.getCurrent().navigate("gruppenarbeiten"));

        H1 gruppenarbeiten = new H1("Gruppenarbeiten");
        gruppenarbeiten.addClassNames("text-l");

        Icon groupIcon = VaadinIcon.GROUP.create();
        groupIcon.setSize("20px");

        gruppenLayout.add(groupIcon, gruppenarbeiten);

        navLayout.add(dashboardLayout, veranstaltungenLayout, teilnehmerLayout, gruppenLayout);


        header.addToMiddle(navLayout);

        header.addToEnd(createMenu());

        addToNavbar(header);
    }

    private Avatar getAvatar() {
        String username = securityService.getAuthenticatedUser().getUsername();
        Benutzer benutzer = benutzerService.findBenutzerByUsername(username);
        if(benutzer != null) {
            if(benutzer.getAvatar() == null)
            {
                benutzer.setAvatar(new Avatar(benutzer.getVorname() +" "+ benutzer.getNachname()));
            }
            return benutzerService.getAvatarByBenutzerId(benutzer.getId());
        }
        else throw new RuntimeException("Benutzer not found");
    }

    private Component createMenu() {
        Avatar logoAvatar = getAvatar();

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menuBar.getStyle().set("margin", "10px");

        MenuItem menuItem = menuBar.addItem(logoAvatar);
        SubMenu subMenu = menuItem.getSubMenu();

        subMenu.addItem(VaadinIcon.COG_O.create(), e-> UI.getCurrent().navigate(ProfileView.class)).add(" Profil");

        if(securityService.checkAuth("ROLE_ADMIN")) {
            subMenu.addItem(VaadinIcon.WRENCH.create(), e -> UI.getCurrent().navigate("/admin")).add(" Admin-Seite");
        }

        subMenu.addItem(VaadinIcon.SIGN_OUT.create(), e -> securityService.logout()).add(" Abmelden");

        return menuBar;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        momPage.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        String fullTitle = getContent().getClass().getAnnotation(PageTitle.class).value();
        fullTitle = fullTitle.replace(" | GroupForge", "");
        return fullTitle;
    }
}
