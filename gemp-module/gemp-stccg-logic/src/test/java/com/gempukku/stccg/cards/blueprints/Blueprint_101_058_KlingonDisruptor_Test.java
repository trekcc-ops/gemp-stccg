package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_058_KlingonDisruptor_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Federation PADD

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void disruptorTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        ST1EGameState gameState = _game.getGameState();
        
        _game.addCardToGame("101_174", _cardLibrary, P1);
        _game.addCardToGame("101_058", _cardLibrary, P1);
        _game.addCardToGame("101_215", _cardLibrary, P1);
        _game.addCardToGame("101_270", _cardLibrary, P1);

        MissionCard mission = null;
        PhysicalReportableCard1E disruptor = null;
        PersonnelCard picard = null;
        PersonnelCard klag = null;

        for (PhysicalCard card : gameState.getAllCardsInGame()) {
            switch(card.getBlueprintId()) {
                case "101_174":
                    mission = (MissionCard) card;
                    break;
                case "101_058":
                    disruptor = (PhysicalReportableCard1E) card;
                    break;
                case "101_215":
                    picard = (PersonnelCard) card;
                    break;
                case "101_270":
                    klag = (PersonnelCard) card;
                    break;
                default:
            }
        }

        assertNotNull(picard);
        assertNotNull(disruptor);
        assertNotNull(klag);

        // Federation Outpost
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_104"));

        assertFalse(outpost.isInPlay());
        assertEquals("Klag", klag.getTitle());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Klingon Disruptor", disruptor.getTitle());

        _game.getGameState().addMissionLocationToSpaceline(mission, 0);
        _game.getGameState().seedFacilityAtLocation(outpost, mission.getLocationDeprecatedOnlyUseForTests());

        assertTrue(outpost.isInPlay());

        picard.reportToFacility(outpost);
        assertEquals(6, picard.getAttribute(CardAttribute.STRENGTH));
        disruptor.reportToFacility(outpost);
        assertEquals(6, picard.getAttribute(CardAttribute.STRENGTH));
        klag.reportToFacility(outpost);
        assertEquals(8, picard.getAttribute(CardAttribute.STRENGTH));
    }
}