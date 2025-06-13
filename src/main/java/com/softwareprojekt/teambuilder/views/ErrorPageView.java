package com.softwareprojekt.teambuilder.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.internal.DefaultErrorHandler;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

//Author: Silas Weber
@DefaultErrorHandler
@Route("error")
@PageTitle("Error | GroupForge")
@AnonymousAllowed
public class ErrorPageView extends VerticalLayout implements HasErrorParameter<RuntimeException> {

    private final H3 subHeader = new H3();

    public ErrorPageView() {
        addClassName("error-view");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
        setPadding(true);
        setSpacing(true);

        Image errorIcon = new Image("images/error.png", "Error Icon");
        errorIcon.setWidth("150px");
        errorIcon.setHeight("150px");


        H1 header = new H1("Oops! Etwas ist schiefgelaufen");
        header.getStyle().set("color", "var(--lumo-error-text-color)");

        subHeader.setText("Die angeforderte Seite konnte nicht gefunden werden");
        subHeader.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button homeButton = new Button("Zurück zur Startseite");
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        add(errorIcon, header, subHeader, homeButton);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<RuntimeException> parameter) {
        Throwable exception = parameter.getCaughtException();

        if (exception instanceof NotFoundException) {
            subHeader.setText("Die angeforderte Seite konnte nicht gefunden werden");
            return HttpServletResponse.SC_NOT_FOUND;
        } else {
            subHeader.setText("Ein unerwarteter Fehler ist aufgetreten");
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }


}
