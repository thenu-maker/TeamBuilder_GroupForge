package com.softwareprojekt.teambuilder.views.gruppenarbeiten;


import com.softwareprojekt.teambuilder.entities.Teilnahme;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

//Author: Thenujan Karunakumar
public class GruppenKachelLayout extends Div {

    /**
     * @param teilnahmen           Liste aller Teilnahmen
     * @param isEditable           Ob Drag-and-Drop erlaubt ist
     * @param onTeilnehmerVerschoben Callback mit Teilnahme-ID und neuem Gruppennamen (nur bei editable = true)
     */
    public GruppenKachelLayout(List<Teilnahme> teilnahmen,
                               boolean isEditable,
                               BiConsumer<Long, String> onTeilnehmerVerschoben,
                               BiConsumer<Long, String> anmerkungChanged) {

        this.getStyle()
                .set("display", "flex")
                .set("flexWrap", "wrap")
                .set("gap", "1.5em")
                .set("justifyContent", "center");

        Map<String, List<Teilnahme>> gruppen = teilnahmen.stream()
                .collect(Collectors.groupingBy(t -> t.getGruppe().getGruppenname()));

        gruppen.entrySet().stream()
                .sorted(Comparator.comparing(e -> extractGroupNumber(e.getKey())))
                .forEach(entry -> {
                    String gruppenname = entry.getKey();
                    List<Teilnahme> teilnahmenDerGruppe = entry.getValue();

                    VerticalLayout gruppeBox = new VerticalLayout();
                    gruppeBox.getStyle()
                            .set("flex", "0 0 calc(33.333% - 1em)")
                            .set("boxSizing", "border-box")
                            .set("padding", "1em")
                            .set("border", "1px solid #ccc")
                            .set("borderRadius", "12px")
                            .set("boxShadow", "2px 2px 6px rgba(0,0,0,0.1)")
                            .set("transition", "transform 0.1s ease-in-out")
                            .set("minWidth", "300px")
                            .set("minHeight", "120px")


                    ;
                    boolean isDark = UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK);
                    String background = isDark
                            ? "linear-gradient(135deg, #1e2a38, #2f3e4d)" // dunkle Farben
                            : "linear-gradient(135deg, #e0eafc, #cfdef3)"; // helle Farben
                    String textColor = isDark ? "white" : "black"; // Passe hier ggf. die Farbe für Dark-Mode an

                    gruppeBox.getStyle()
                            .set("background", background)
                            .set("color", textColor);

                    gruppeBox.getElement().executeJs("""
                        this.addEventListener('mouseenter', () => this.style.transform = 'scale(1.02)');
                        this.addEventListener('mouseleave', () => this.style.transform = 'scale(1)');
                    """);

                  String anmerkung = teilnahmenDerGruppe.getFirst().getGruppe().getAnmerkung();

                  HorizontalLayout titelLayout = new HorizontalLayout();
                  H4 gruppenTitelLabel = new H4(gruppenname);
                  titelLayout.add(gruppenTitelLabel);

                  Span anmerkungLabel;
                  if (anmerkung != null && !anmerkung.isBlank()) {
                      anmerkungLabel = new Span(anmerkung);
                      anmerkungLabel.getStyle().set("color", "#888").set("fontStyle", "italic").set("marginLeft", "8px");
                      titelLayout.add(anmerkungLabel);
                  } else if (isEditable) {
                      anmerkungLabel = new Span("Anmerkung hinzufügen...");
                      anmerkungLabel.getStyle().set("color", "#888").set("fontStyle", "italic").set("marginLeft", "8px").set("cursor", "pointer");
                      titelLayout.add(anmerkungLabel);
                  } else {
                      anmerkungLabel = null;
                  }

                  gruppeBox.add(titelLayout);

                    if (isEditable && anmerkungLabel != null) {
                        final Span[] aktuellesLabel = {anmerkungLabel};

                        ComponentEventListener<ClickEvent<Span>> clickListener = new ComponentEventListener<>() {
                            @Override
                            public void onComponentEvent(ClickEvent<Span> e) {
                                TextField anmerkungField = new TextField();
                                anmerkungField.setMaxLength(250);
                                anmerkungField.setValue(teilnahmenDerGruppe.getFirst().getGruppe().getAnmerkung() != null
                                        ? teilnahmenDerGruppe.getFirst().getGruppe().getAnmerkung() : "");
                                anmerkungField.setWidth("200px");
                                titelLayout.replace(aktuellesLabel[0], anmerkungField);
                                anmerkungField.focus();

                                Runnable saveAnmerkung = () -> {
                                    String neueAnmerkung = anmerkungField.getValue();
                                    Span neuesLabel;
                                    if (neueAnmerkung.isBlank()) {
                                        neuesLabel = new Span("Anmerkung hinzufügen...");
                                        neuesLabel.getStyle().set("color", "#888").set("fontStyle", "italic").set("marginLeft", "8px").set("cursor", "pointer");
                                    } else {
                                        neuesLabel = new Span(neueAnmerkung);
                                        neuesLabel.getStyle().set("color", "#888").set("fontStyle", "italic").set("marginLeft", "8px");
                                    }
                                    titelLayout.replace(anmerkungField, neuesLabel);
                                    aktuellesLabel[0] = neuesLabel;
                                    teilnahmenDerGruppe.getFirst().getGruppe().setAnmerkung(neueAnmerkung);

                                    if (anmerkungChanged != null) {
                                        anmerkungChanged.accept(
                                                teilnahmenDerGruppe.getFirst().getGruppe().getId(),
                                                neueAnmerkung
                                        );
                                    }

                                    if (isEditable) {
                                        neuesLabel.addClickListener(this);
                                    }
                                };

                                anmerkungField.addBlurListener(ev -> saveAnmerkung.run());
                                anmerkungField.addKeyDownListener(Key.ENTER, ev -> anmerkungField.blur());
                            }
                        };

                        anmerkungLabel.addClickListener(clickListener);
                    }

                   if (isEditable) {
                       DropTarget<VerticalLayout> dropTarget = DropTarget.create(gruppeBox);
                       dropTarget.addDropListener(event -> {
                           event.getDragSourceComponent().ifPresent(dragged -> {
                               if (dragged instanceof Span draggedSpan) {
                                   String teilnahmeIdStr = draggedSpan.getElement().getProperty("teilnahmeId");
                                   Long teilnahmeId = Long.parseLong(teilnahmeIdStr);
                                   HorizontalLayout titelLayoutneu = (HorizontalLayout) gruppeBox.getComponentAt(0);
                                   H4 gruppenTitelLabelneu = (H4) titelLayoutneu.getComponentAt(0);
                                   String neueGruppe = gruppenTitelLabelneu.getText();                                   onTeilnehmerVerschoben.accept(teilnahmeId, neueGruppe);
                                   gruppeBox.add(draggedSpan);
                               }
                           });
                       });
                   }

                    for (Teilnahme t : teilnahmenDerGruppe) {
                        Span teilnehmerSpan = new Span();
                        Icon userIcon = VaadinIcon.USER.create();
                        userIcon.setSize("20px");
                        userIcon.getStyle().set("marginBottom", "4px");
                        teilnehmerSpan.add(userIcon, new Span(" " + t.getTeilnehmer().getVorname() + " " + t.getTeilnehmer().getNachname()));
                        teilnehmerSpan.getStyle().set("marginBottom", "4px");
                        teilnehmerSpan.getElement().setProperty("teilnahmeId", t.getId());

                        if (isEditable) {
                            DragSource.create(teilnehmerSpan);
                        }

                        gruppeBox.add(teilnehmerSpan);
                    }

                    this.add(gruppeBox);
                });
    }

    private int extractGroupNumber(String gruppenname) {
        try {
            return Integer.parseInt(gruppenname.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }



}

