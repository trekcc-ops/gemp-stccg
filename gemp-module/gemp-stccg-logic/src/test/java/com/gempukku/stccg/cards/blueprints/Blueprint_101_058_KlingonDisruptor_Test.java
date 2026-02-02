package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_058_KlingonDisruptor_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard disruptor;
    private PersonnelCard picard;
    private PersonnelCard klag;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        disruptor = builder.addCardInHand("101_058", "Klingon Disruptor", P1, EquipmentCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        klag = builder.addCardInHand("101_270", "Klag", P1, PersonnelCard.class);
    }
    
    @Test
    public void disruptorTest() throws Exception {
        initializeGame();

        assertTrue(outpost.isInPlay());
        assertFalse(klag.isInPlay());
        assertFalse(picard.isInPlay());
        assertFalse(disruptor.isInPlay());

        reportCardToFacility(picard, outpost);
        assertEquals(6, picard.getStrength(_game));
        reportCardToFacility(disruptor, outpost);
        assertEquals(6, picard.getStrength(_game));
        reportCardToFacility(klag, outpost);
        assertEquals(8, picard.getStrength(_game));
    }
}