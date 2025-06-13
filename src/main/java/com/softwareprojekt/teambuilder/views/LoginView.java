package com.softwareprojekt.teambuilder.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

//Author: Silas Weber
@Route("login")
@PageTitle("Login | GroupForge")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver  {

    private final LoginForm loginForm;
    private LoginI18n i18n;

    public LoginView() {

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getElement().executeJs(
                "document.body.style.backgroundImage = 'url(/images/Login_padlock.jpg)';" +
                        "document.body.style.backgroundSize = 'cover';" +
                        "document.body.style.backgroundPosition = 'center';" +
                        "document.body.style.backgroundRepeat = 'no-repeat';" +
                        "document.body.style.margin = '0';" +
                        "document.body.style.height = '100vh';"
        );

        Div loginOverlay = new Div();
        loginOverlay.getStyle().setAlignItems(Style.AlignItems.CENTER);
        loginOverlay.getElement().getStyle()
                .set("background-color", "rgba(255, 255, 255, 0.9)")
                .set("border-radius", "8px")
                .set("padding", "20px");

        loginForm = new LoginForm();
        loginForm.setAction("login");

        ueberSetzeAufDeutsch();
        loginForm.setI18n(i18n);

        loginForm.addForgotPasswordListener(e -> showAdminContactNotification());

        Image image = new Image("images/logo-gf.png", "GroupForge");
        image.setWidth("450px");
        image.setHeight("300px");
        image.getStyle().setMarginBottom("5px");
        image.getStyle().setAlignSelf(Style.AlignSelf.CENTER);

        loginOverlay.add(image, loginForm);

        add(loginOverlay);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

    private void ueberSetzeAufDeutsch(){
        i18n = LoginI18n.createDefault();

        LoginI18n.Header i18nHeader = new LoginI18n.Header();
        i18nHeader.setTitle("Anmelden bei GroupForge");
        i18n.setHeader(i18nHeader);

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Anmelden");
        i18nForm.setUsername("Benutzername");
        i18nForm.setPassword("Passwort");
        i18nForm.setSubmit("Anmelden");
        i18nForm.setForgotPassword("Passwort vergessen?");
        i18n.setForm(i18nForm);
    }

    private void showAdminContactNotification() {
        Notification notification = Notification.show(
                "Bitte wenden Sie sich an Ihren Administrator, um Ihr Passwort zurückzusetzen.",
                3500,
                Notification.Position.MIDDLE
        );
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notification.open();
    }


}
