package com.softwareprojekt.teambuilder.views.gruppenarbeiten;

import com.softwareprojekt.teambuilder.entities.Gruppe;
import com.softwareprojekt.teambuilder.entities.Gruppenarbeit;
import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.softwareprojekt.teambuilder.services.GruppeService;
import com.softwareprojekt.teambuilder.services.GruppenarbeitService;
import com.softwareprojekt.teambuilder.services.TeilnahmeService;
import com.softwareprojekt.teambuilder.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Author: Silas Weber
@Route(value = "/gruppenarbeit/:gruppenarbeitId/bewerten", layout = MainLayout.class)
@PageTitle("Gruppenbewertung | GroupForge")
@PermitAll
public class GruppenarbeitGruppenBewertenView extends VerticalLayout implements BeforeEnterObserver {
    GruppenarbeitService gruppenarbeitService;
    GruppeService gruppeService;
    TeilnahmeService teilnahmeService;

    List<Component> grids = new ArrayList<>();

    Gruppenarbeit gruppenarbeit;

    String titel;

    public GruppenarbeitGruppenBewertenView(GruppenarbeitService gruppenarbeitService,
                                            GruppeService gruppeService,
                                            TeilnahmeService teilnahmeService) {
        this.gruppenarbeitService = gruppenarbeitService;
        this.gruppeService = gruppeService;
        this.teilnahmeService = teilnahmeService;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var routeParams = event.getRouteParameters();

        String gruppenarbeitId = routeParams.get("gruppenarbeitId").orElse(null);

        if (gruppenarbeitId == null) {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }

        gruppenarbeit = gruppenarbeitService.findGruppenarbeitById(Long.parseLong(gruppenarbeitId));
        titel = gruppenarbeit.getTitel();

        if (gruppenarbeit != null) {

            // Überschrift und Semesteranzeige immer oben fixieren
            H1 title = new H1("Gruppenarbeit: " + gruppenarbeit.getTitel());
            title.addClassNames("text-xl");

            Button backButton = new Button("Zurück zur Gruppenübersicht");
            backButton.addClickListener(e -> UI.getCurrent().navigate("/gruppenarbeit/"+gruppenarbeitId+"/anzeige"));

            add(
                    backButton,
                    title
            );
            getGruppen();
        } else {
            throw new IllegalArgumentException("Ungültige Gruppenarbeit-ID oder Gruppenarbeit nicht gefunden.");

        }
    }

   public void getGruppen() {
        List<Gruppe> gruppen = gruppeService.findAllByGruppenarbeit(gruppenarbeit);

        gruppen.sort(Comparator.comparing(Gruppe::getGruppenname, String.CASE_INSENSITIVE_ORDER));

        for (Gruppe gruppe : gruppen) {
            grids.add(createGrid(gruppe));
        }

        add(grids);
    }

    public Component createGrid(Gruppe gruppe) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        HorizontalLayout labelLayout = new HorizontalLayout();

        H1 name = new H1(gruppe.getGruppenname());
        name.addClassNames("text-l");
        labelLayout.add(name);


        if (gruppe.getAnmerkung() != null && !gruppe.getAnmerkung().isEmpty()) {
            Span anmerkungLabel = new Span(gruppe.getAnmerkung());
            anmerkungLabel.getStyle().set("color", "#888").set("fontStyle", "italic").set("marginLeft", "8px").set("cursor", "pointer");
            labelLayout.add(anmerkungLabel);
        }
        else {
            name.setText(gruppe.getGruppenname());
        }

        Button bewertenButton = new Button("Einheitliche Bewertung", e -> createBewertungsDialog(gruppe));
        bewertenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        header.add(
                labelLayout,
                bewertenButton
        );

        header.expand(labelLayout);

        Grid<Teilnahme> grid = new Grid<>(Teilnahme.class, false);
        grid.setSizeFull();
        grid.setAllRowsVisible(true);


        grid.addColumn(t -> t.getTeilnehmer().getMatrnr()).setHeader("Matrikelnummer").setSortable(true);
        grid.addColumn(t -> t.getTeilnehmer().getVorname() + " " + t.getTeilnehmer().getNachname()).setHeader("Teilnehmer").setSortable(true);
        Grid.Column<Teilnahme> praesentationspunkteColumn = grid.addColumn(t -> t.getPraesentationspunkte()).setHeader("Praesentationspunkte");
        Grid.Column<Teilnahme> leistungspunkteColumn = grid.addColumn(t -> t.getLeistungspunkte()).setHeader("Leistungspunkte");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect();

        Binder<Teilnahme> binder = new Binder<>(Teilnahme.class);
        Editor<Teilnahme> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.addSaveListener(event -> teilnahmeService.save(event.getItem()));

        NumberField praesentationspunkteField = new NumberField();
        praesentationspunkteField.setWidthFull();
        praesentationspunkteField.addValueChangeListener(event -> {
            editor.getItem().setPraesentationspunkte(event.getValue());
            teilnahmeService.save(editor.getItem());
        });
        addCloseHandler(praesentationspunkteField, editor);
        binder.forField(praesentationspunkteField)
                .bind(Teilnahme::getPraesentationspunkte, Teilnahme::setPraesentationspunkte);
        praesentationspunkteColumn.setEditorComponent(praesentationspunkteField);

        NumberField leistungspunkteField = new NumberField();
        leistungspunkteField.setWidthFull();
        leistungspunkteField.addValueChangeListener(event -> {
            editor.getItem().setLeistungspunkte(event.getValue());
            teilnahmeService.save(editor.getItem());
        });
        addCloseHandler(leistungspunkteField, editor);
        binder.forField(leistungspunkteField)
                .bind(Teilnahme::getLeistungspunkte, Teilnahme::setLeistungspunkte);
        leistungspunkteColumn.setEditorComponent(leistungspunkteField);

        grid.addItemClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });

        List<Teilnahme> teilnahmen = teilnahmeService.findAllByGruppe(gruppe);
        teilnahmen.sort((t1, t2) -> t1.getId()>(t2.getId()) ? 1 : -1);
        grid.setItems(teilnahmen);

        layout.add(
                header,
                grid
        );
        return layout;
    }

    private static void addCloseHandler(Component textField,
                                        Editor<Teilnahme> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
    }

    private void createBewertungsDialog(Gruppe gruppe) {
        Dialog bewerteAll = new Dialog();

        bewerteAll.setHeaderTitle("Präsentationspunkte für Gruppe " + gruppe.getGruppenname());

        VerticalLayout body = new VerticalLayout();

        body.setSizeFull();

        NumberField value = new NumberField("Präsentationspunkte");
        value.setRequired(true);
        value.setAutofocus(true);
        value.setStepButtonsVisible(true);
        value.setStep(0.1);
        value.setMin(0.0);
        value.setValue(0.0);

        body.add(
            value
        );

        bewerteAll.add(body);

        Button saveButton = new Button("Save");
        saveButton.addClickListener(e -> {
            for (Teilnahme teilnahme : gruppe.getTeilnahmen())
            {
                teilnahme.setPraesentationspunkte(value.getValue());
                teilnahmeService.save(teilnahme);
            }
            UI.getCurrent().getPage().reload();
            bewerteAll.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button("Cancel", e -> bewerteAll.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        bewerteAll.getFooter().add(cancelButton);
        bewerteAll.getFooter().add(saveButton);

        bewerteAll.open();
    }

}
