package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractLogicTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CardBlueprintLibraryTest extends AbstractLogicTest {

    // TODO - Add check that all card images are valid URLs
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
