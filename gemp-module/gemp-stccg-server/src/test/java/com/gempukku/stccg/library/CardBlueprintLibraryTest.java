package com.gempukku.stccg.library;

import com.gempukku.stccg.AbstractLogicTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class CardBlueprintLibraryTest extends AbstractLogicTest {

    @Test
    public void AllBlueprintsAreBuilt() {
        assertTrue(_cardLibrary.checkLoadSuccess());
    }
    @Test
    public void LibraryLoadsWithNoDuplicates() {
        Map<String, String> cardNames = new HashMap<>();
        for (int i = 0; i <= 19; i++) {
            for (int j = 1; j <= 365; j++) {
                String blueprintId = i + "_" + j;
                try {
                    if (blueprintId.equals(_cardLibrary.getBaseBlueprintId(blueprintId))) {
                        try {
                            CardBlueprint cardBlueprint = _cardLibrary.getCardBlueprint(blueprintId);
                            String cardName = cardBlueprint.getFullName();
                            if (cardNames.containsKey(cardName) && cardBlueprint.getCardType() != CardType.SITE)
                                System.out.println("Multiple detected - " + cardName + ": " + cardNames.get(cardName) + " and " + blueprintId);
                            else
                                cardNames.put(cardName, blueprintId);
                        }
                        catch(CardNotFoundException ex) {
                            break;
                        }
                    }
                } catch (IllegalArgumentException exp) {
                    //exp.printStackTrace();
                }
            }
        }

    }
}
