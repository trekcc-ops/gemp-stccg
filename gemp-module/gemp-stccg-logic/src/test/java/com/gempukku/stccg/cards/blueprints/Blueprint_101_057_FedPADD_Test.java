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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_101_057_FedPADD_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard padd;
    private PersonnelCard picard;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        padd = builder.addCardInHand("101_057", "Federation PADD", P1, EquipmentCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
    }

    @Test
    public void cunningTest() throws Exception {
        initializeGame();

        reportCardsToFacility(outpost, picard);
        assertEquals(8, picard.getCunning(_game));

        reportCardsToFacility(outpost, padd);
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(picard, padd));
        assertEquals(10, picard.getCunning(_game));
    }
}