package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Veranstaltung;
import com.softwareprojekt.teambuilder.security.SecurityService;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import com.softwareprojekt.teambuilder.services.SemesterService;
import com.softwareprojekt.teambuilder.services.VeranstaltungService;
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
public class VeranstaltungsVerwaltungComponent extends VerticalLayout {

    Grid<Veranstaltung> veranstaltungsGrid =new Grid<>(Veranstaltung.class, false);
    TextField filterText= new TextField();
    VeranstaltungsForm form;
    private final VeranstaltungService veranstaltungService;
    private final SecurityService securityService;
    private final BenutzerService benutzerService;
    private final SemesterService semesterService;

    public VeranstaltungsVerwaltungComponent(VeranstaltungService veranstaltungService,
                                             BenutzerService benutzerService,
                                             SecurityService securityService,
                                             SemesterService semesterService) {
        this.veranstaltungService = veranstaltungService;
        this.securityService = securityService;
        this.benutzerService = benutzerService;
        this.semesterService = semesterService;
        
        setSizeFull();

        H1 title = new H1("Veranstaltungs-Verwaltung");

        configureVeranstaltungsGrid();
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
        form.setVeranstaltung(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList(){
        veranstaltungsGrid.setItems(veranstaltungService.findAllVeranstaltungen(filterText.getValue()));
    }

    private Component getContent(){
        HorizontalLayout content = new HorizontalLayout(veranstaltungsGrid, form);
        content.setFlexGrow(2, veranstaltungsGrid);
        content.setFlexGrow(1, form);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private void configureForm() {
        form = new VeranstaltungsForm(benutzerService, semesterService);
        form.setWidth("25em");

        form.addListener(VeranstaltungsForm.SaveEvent.class, this::saveContact);
        form.addListener(VeranstaltungsForm.DeleteEvent.class, this::deleteEvent);
        form.addListener(VeranstaltungsForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveContact(VeranstaltungsForm.SaveEvent event) {
        veranstaltungService.save(event.getVeranstaltung());
        updateList();
        closeEditor();
    }

    private void deleteEvent(VeranstaltungsForm.DeleteEvent event) {

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Veranstaltung löschen?");
        dialog.setText("Möchten Sie die Veranstaltung ("+ event.getVeranstaltung().getTitel() +") wirklich löschen?");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            veranstaltungService.delete(event.getVeranstaltung());
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

        Button addButton = new Button("Hinzufügen", VaadinIcon.PLUS.create());
        addButton.addClickListener(event -> addVeranstaltung());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addButton);

        return toolbar;
    }

    private void addVeranstaltung() {
        veranstaltungsGrid.asSingleSelect().clear();
        editVeranstaltung(new Veranstaltung());
    }


    private void configureVeranstaltungsGrid(){
        veranstaltungsGrid.setSizeFull();
        veranstaltungsGrid.setHeightFull();

        veranstaltungsGrid.addColumn(Veranstaltung::getTitel).setHeader("Titel").setSortable(true);
        veranstaltungsGrid.addColumn(Veranstaltung::getSemester).setHeader("Semester").setSortable(true);
        veranstaltungsGrid.addColumn(v -> v.getTeilnehmer().size()).setHeader("Anzahl Teilnehmer").setSortable(true);
        veranstaltungsGrid.addColumn(v -> v.getBenutzer().getUsername()).setHeader("Veranstalter").setSortable(true);

        veranstaltungsGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        veranstaltungsGrid.asSingleSelect().addValueChangeListener(event -> editVeranstaltung(event.getValue()));
    }

    private void editVeranstaltung(Veranstaltung veranstaltung) {
        if (veranstaltung == null) {
            closeEditor();
        } else {
            form.setVeranstaltung(veranstaltung);
            form.setVisible(true);
            addClassName("editing");
        }
    }

}
