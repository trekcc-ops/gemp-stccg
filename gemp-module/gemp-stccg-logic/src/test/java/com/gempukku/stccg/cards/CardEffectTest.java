package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CardEffectTest {

    private boolean showListInConsole = true;

    @Test
    public void drawEffectTest() {
        CardBlueprintLibrary library = new CardBlueprintLibrary();
        List<CardBlueprint> blueprints = new ArrayList<>();
        for (CardBlueprint blueprint : library.getAllBlueprints()) {
            if (blueprint.hasDrawCardEffect()) {
                blueprints.add(blueprint);
            }
        }
        assertFalse(blueprints.isEmpty());
        if (showListInConsole) {
            System.out.println("Cards with draw effect:");
            for (CardBlueprint blueprint : blueprints) {
                System.out.println("  " + blueprint.getTitle());
            }
        }
    }

    @Test
    public void playForFreeEffectTest() {
        CardBlueprintLibrary library = new CardBlueprintLibrary();
        List<CardBlueprint> blueprints = new ArrayList<>();
        for (CardBlueprint blueprint : library.getAllBlueprints()) {
            if (blueprint.hasPlayCardForFreeEffect()) {
                blueprints.add(blueprint);
            }
        }
        assertFalse(blueprints.isEmpty());
        if (showListInConsole) {
            System.out.println("Cards with play for free effect:");
            for (CardBlueprint blueprint : blueprints) {
                System.out.println("  " + blueprint.getTitle());
            }
        }
    }


}