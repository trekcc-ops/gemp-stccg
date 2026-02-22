package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.SetDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("MagicNumber")
public class CardBlueprintLibraryTest {

    private final CardBlueprintLibrary _cardLibrary = new CardBlueprintLibrary();
    // Commented out lackey validation so that it isn't run every single time the server is built

    // TODO - Add check that all card images are valid URLs
    @Test
    public void AllBlueprintsAreBuilt() throws CardNotFoundException {
        assertTrue(_cardLibrary.checkLoadSuccess());
        assertEquals("Admiral McCoy", _cardLibrary.getCardBlueprint("106_014").getTitle());
        int totalCardCount = 0;

        for (SetDefinition set : _cardLibrary.getSetDefinitions().values()) {
/*            StringBuilder sb = new StringBuilder();
            sb.append("Set ").append(set.getSetId()).append(" '").append(set.getSetName());
            sb.append("' - ").append(TextUtils.plural(set.getAllCards().size(), "card"));

            GameType gameType = set.getGameType();
            String gameTypeName = (gameType == null) ? "null" : gameType.name();

            sb.append(" [").append(gameTypeName).append("]");
            System.out.println(sb); */
            totalCardCount += set.getAllCards().size();
        }

        assertEquals(totalCardCount, _cardLibrary.getAllBlueprintIds().size());
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

        assertTrue(_cardLibrary.hasAlternateInSet("101_218", "113"));
        assertEquals(2, _cardLibrary.getAllAlternates("113_003").size());
    }


}