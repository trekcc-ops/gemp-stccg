package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_095_WillRiker_Test extends AbstractAtTest {

    @Test
    public void incompatibilityTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        PersonnelCard riker = builder.addCardInHand("155_095", "Will Riker", P1, PersonnelCard.class);
        ShipCard runabout = builder.addCardInHand("101_331", "Runabout", P1, ShipCard.class);
        PersonnelCard picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        builder.startGame();
        assertTrue(riker.isCompatibleWith(_game, riker));
        assertTrue(riker.isCompatibleWith(_game, runabout));
        assertFalse(riker.isCompatibleWith(_game, picard));
    }
}