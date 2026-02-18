package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_163_047_TheOne_Test extends AbstractAtTest {

    @Test
    public void incompatibilityTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        PersonnelCard theOne = builder.addCardInHand("163_047", "The One", P1, PersonnelCard.class);
        PersonnelCard baran = builder.addCardInHand("101_290", "Baran", P1, PersonnelCard.class);
        PersonnelCard picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        builder.startGame();
        assertTrue(theOne.isCompatibleWith(_game, baran));
        assertTrue(picard.isCompatibleWith(_game, baran));
        assertFalse(theOne.isCompatibleWith(_game, picard));
    }
}