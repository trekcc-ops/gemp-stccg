package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractLogicTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardBlueprintLibraryTest extends AbstractLogicTest {

    // TODO - Add check that all card images are valid URLs
    // TODO - Remove this later
    @Test
    public void AllBlueprintsAreBuilt() {
        assertTrue(_cardLibrary.checkLoadSuccess());
        try {
            assertEquals("Admiral McCoy", _cardLibrary.getCardBlueprint("106_014").getTitle());
        } catch(CardNotFoundException exp) {
            System.out.println("Invalid card definition");
        }
    }

    @Test
    public void RarityCheck() {
        System.out.println(_cardLibrary.getSetDefinitions().get("101").getCardsOfRarity("R").size());
        System.out.println(_cardLibrary.getSetDefinitions().get("101").getCardsOfRarity("U").size());
        System.out.println(_cardLibrary.getSetDefinitions().get("101").getCardsOfRarity("C").size());
    }
}
