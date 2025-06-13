package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

//Author: Silas Weber
public class BenutzerverwaltungComponent extends VerticalLayout {

    Grid<Benutzer> benutzerGrid =new Grid<>(Benutzer.class, false);
    TextField filterText= new TextField();
    BenutzerForm form;
    private final BenutzerService benutzerService;
    private final SecurityService securityService;

    public BenutzerverwaltungComponent(BenutzerService benutzerService,
                                       SecurityService securityService) {
        this.benutzerService = benutzerService;
        this.securityService = securityService;

        setSizeFull();

        H1 title = new H1("Benutzer-Verwaltung");

        configureBenutzerGrid();
        configureForm();

        add(
                title,
                getToolbar(),
                getContent()
        );

        updateList();
        closeEditor();
    }

    private void closeEditor() {
        form.setBenutzer(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList(){
        benutzerGrid.setItems(benutzerService.findAllBenutzer(filterText.getValue()));
    }

    private Component getContent(){
        HorizontalLayout content = new HorizontalLayout(benutzerGrid, form);
        content.setFlexGrow(2, benutzerGrid);
        content.setFlexGrow(1, form);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private void configureForm() {
        form = new BenutzerForm(benutzerService,securityService);
        form.setWidth("25em");

        form.addListener(BenutzerForm.SaveEvent.class, this::saveContact);
        form.addListener(BenutzerForm.DeleteEvent.class, this::deleteEvent);
        form.addListener(BenutzerForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveContact(BenutzerForm.SaveEvent event) {
        benutzerService.saveBenutzer(event.getBenutzer());
        updateList();
        closeEditor();
    }

    private void deleteEvent(BenutzerForm.DeleteEvent event) {

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Benutzer löschen?");
        dialog.setText("Möchten Sie den Benutzer ("+ event.getBenutzer().getUsername() +") wirklich löschen?");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            benutzerService.deleteBenutzer(event.getBenutzer());
            updateList();
            closeEditor();
        });

        dialog.open();
    }

    private Component getToolbar(){
        filterText.setPlaceholder("Suche");
        filterText.setClearButtonVisible(true);
        filterText.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(event -> {updateList();});

        Button addBenutzerButton = new Button("Hinzufügen", VaadinIcon.PLUS.create());
        addBenutzerButton.addClickListener(event -> addBenutzer());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addBenutzerButton);

        return toolbar;
    }

    private void addBenutzer() {
        benutzerGrid.asSingleSelect().clear();
        editBenutzer(new Benutzer());
    }


    private void configureBenutzerGrid(){
        benutzerGrid.addClassNames("benutzer-grid");
        benutzerGrid.setSizeFull();
        benutzerGrid.setHeightFull();

        benutzerGrid.addColumn(Benutzer::getUsername).setHeader("Username").setSortable(true);
        benutzerGrid.addColumn(Benutzer::getTitel).setHeader("Titel").setSortable(true);
        benutzerGrid.addColumn(Benutzer::getVorname).setHeader("Vorname").setSortable(true);
        benutzerGrid.addColumn(Benutzer::getNachname).setHeader("Nachname").setSortable(true);
        benutzerGrid.addColumn(benutzer -> benutzer.getRole().toString()).setHeader("Role").setSortable(true);
        benutzerGrid.addColumn(benutzer -> benutzer.getVeranstaltungen().size()).setHeader("Anzahl Veranstaltungen").setSortable(true);

        benutzerGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        benutzerGrid.asSingleSelect().addValueChangeListener(event -> editBenutzer(event.getValue()));
    }

    private void editBenutzer(Benutzer benutzer) {
        if (benutzer == null) {
            closeEditor();
        } else {
            form.setBenutzer(benutzer);
            form.setVisible(true);
            addClassName("editing");
        }
    }

}
