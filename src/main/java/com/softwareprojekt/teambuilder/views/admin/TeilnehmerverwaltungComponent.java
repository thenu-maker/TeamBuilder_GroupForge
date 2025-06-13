package com.softwareprojekt.teambuilder.views.admin;

import com.softwareprojekt.teambuilder.entities.Teilnehmer;
import com.softwareprojekt.teambuilder.services.TeilnehmerService;
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
//Author: Tolga Cenk Kilic
public class TeilnehmerverwaltungComponent extends VerticalLayout {

    Grid<Teilnehmer> teilnehmerGrid =new Grid<>(Teilnehmer.class, false);
    TextField filterText= new TextField();
    TeilnehmerForm form;
    
    private final TeilnehmerService teilnehmerService;

    public TeilnehmerverwaltungComponent(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;

        setSizeFull();
    
        H1 title = new H1("Teilnehmer-Verwaltung");
    
        configureTeilnehmerGrid();
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
        form.setTeilnehmer(null);
        form.setVisible(false);
        removeClassName("editing");
    }
    
    private void updateList(){
        teilnehmerGrid.setItems(teilnehmerService.findAllBenutzer(filterText.getValue()));
    }
    
    private Component getContent(){
        HorizontalLayout content = new HorizontalLayout(teilnehmerGrid, form);
        content.setFlexGrow(2, teilnehmerGrid);
        content.setFlexGrow(1, form);
        content.addClassName("content");
        content.setSizeFull();
    
        return content;
    }
    
    private void configureForm() {
        form = new TeilnehmerForm();
        form.setWidth("25em");
    
        form.addListener(TeilnehmerForm.SaveEvent.class, this::saveContact);
        form.addListener(TeilnehmerForm.DeleteEvent.class, this::deleteEvent);
        form.addListener(TeilnehmerForm.CloseEvent.class, e -> closeEditor());
    }
    
    private void saveContact(TeilnehmerForm.SaveEvent event) {
        teilnehmerService.saveTeilnehmer(event.getTeilnehmer());
        updateList();
        closeEditor();
    }
    
    private void deleteEvent(TeilnehmerForm.DeleteEvent event) {
    
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Teilnehmer löschen?");
        dialog.setText("Möchten Sie den Teilnehmer ("+ event.getTeilnehmer().getVorname() + " " + event.getTeilnehmer().getNachname() +") wirklich löschen?");
    
        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");
    
        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            teilnehmerService.deleteTeilnehmerMitAbhaengigkeiten(event.getTeilnehmer());
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
        addButton.addClickListener(event -> addTeilnehmer());
    
        HorizontalLayout toolbar = new HorizontalLayout(filterText, addButton);
    
        return toolbar;
    }
    
    private void addTeilnehmer() {
        teilnehmerGrid.asSingleSelect().clear();
        editTeilnehmer(new Teilnehmer(1L));
    }
    
    
    private void configureTeilnehmerGrid(){
        teilnehmerGrid.addClassNames("benutzer-grid");
        teilnehmerGrid.setSizeFull();
        teilnehmerGrid.setHeightFull();
    
        teilnehmerGrid.addColumn(Teilnehmer::getMatrnr).setHeader("Matrikelnummer").setSortable(true);
        teilnehmerGrid.addColumn(Teilnehmer::getNachname).setHeader("Nachname").setSortable(true);
        teilnehmerGrid.addColumn(Teilnehmer::getVorname).setHeader("Vorname").setSortable(true);
        teilnehmerGrid.addColumn(t -> t.getVeranstaltungen().size()).setHeader("Anzahl Veranstaltungen").setSortable(true);

        teilnehmerGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    
        teilnehmerGrid.asSingleSelect().addValueChangeListener(event -> editTeilnehmer(event.getValue()));
    }
    
    private void editTeilnehmer(Teilnehmer teilnehmer) {
        if (teilnehmer == null) {
            closeEditor();
        } else {
            form.setTeilnehmer(teilnehmer);
            form.setVisible(true);
            addClassName("editing");
        }
    }
}
