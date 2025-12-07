package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_057_FedPADD_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Federation PADD

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void fedPADDTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        ST1EGameState gameState = _game.getGameState();
        
        _game.addCardToGame("101_174", P1);
        _game.addCardToGame("101_057", P1);
        _game.addCardToGame("101_215", P1);

        MissionCard mission = null;
        EquipmentCard padd = null;
        PersonnelCard picard = null;

        for (PhysicalCard card : gameState.getAllCardsInGame()) {
            switch(card.getBlueprintId()) {
                case "101_174":
                    mission = (MissionCard) card;
                    break;
                case "101_057":
                    padd = (EquipmentCard) card;
                    break;
                case "101_215":
                    picard = (PersonnelCard) card;
                    break;
                default:
            }
        }

        assertNotNull(picard);
        assertNotNull(padd);

        // Federation Outpost
        FacilityCard outpost = (FacilityCard) newCardForGame("101_104", P1);

        assertFalse(outpost.isInPlay());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Federation PADD", padd.getTitle());
        assertNotNull(mission);

        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission);

        assertTrue(outpost.isInPlay());

        reportCardsToFacility(outpost, picard);
        assertEquals(8, picard.getCunning(_game));

        reportCardsToFacility(outpost, padd);
        assertTrue(gameState.cardsArePresentWithEachOther(picard, padd));
        assertEquals(10, picard.getCunning(_game));

    }
}