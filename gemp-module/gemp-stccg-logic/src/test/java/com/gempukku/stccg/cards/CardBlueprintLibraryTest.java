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

    @Test
    public void alternateBlueprintCheck() throws CardNotFoundException {
        /* The blueprint IDs below were arbitrarily selected. At time of writing of this test, no card blueprints are
            using base blueprints. If blueprints do start to use this property in the future, this test will need to
            be rewritten.
         */

        _cardLibrary.getCardBlueprint("101_218").setBaseBlueprintId("101_217");
        _cardLibrary.getCardBlueprint("113_003").setBaseBlueprintId("101_217");

        _cardLibrary.reloadMappings();

        assertEquals("101_217", _cardLibrary.getBaseBlueprintId("101_218"));
        assertEquals("101_217", _cardLibrary.getBaseBlueprintId("113_003"));

        assertTrue(_cardLibrary.hasAlternateInSet("101_218", 113));
        assertEquals(2, _cardLibrary.getAllAlternates("113_003").size());
    }
}
