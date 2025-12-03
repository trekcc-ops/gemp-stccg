package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_060_MedicalKit_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Medical Kit

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void medicalKitTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        ST1EGameState gameState = _game.getGameState();
        
        _game.addCardToGame("101_174", _cardLibrary, P1);
        _game.addCardToGame("101_060", _cardLibrary, P1);
        _game.addCardToGame("101_215", _cardLibrary, P1);
        _game.addCardToGame("105_085", _cardLibrary, P1);

        MissionCard mission = null;
        PhysicalReportableCard1E medicalKit = null;
        PersonnelCard picard = null;
        PersonnelCard taris = null;

        for (PhysicalCard card : gameState.getAllCardsInGame()) {
            switch(card.getBlueprintId()) {
                case "101_174":
                    mission = (MissionCard) card;
                    break;
                case "101_060":
                    medicalKit = (PhysicalReportableCard1E) card;
                    break;
                case "101_215":
                    picard = (PersonnelCard) card;
                    break;
                case "105_085":
                    taris = (PersonnelCard) card;
                    break;
                default:
            }
        }

        // Federation Outpost
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_104"));

        assertFalse(outpost.isInPlay());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Taris", taris.getTitle());
        assertEquals("Medical Kit", medicalKit.getTitle());
        assertNotNull(mission);

        gameState.addMissionLocationToSpaceline(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocation(_game, outpost, mission.getGameLocation());

        assertTrue(outpost.isInPlay());

        picard.reportToFacility(outpost);
        taris.reportToFacility(outpost);

        assertEquals(0, picard.getSkillLevel(SkillName.MEDICAL));
        assertEquals(1, taris.getSkillLevel(SkillName.MEDICAL));

        medicalKit.reportToFacility(outpost);
        assertTrue(medicalKit.isPresentWith(picard));
        assertTrue(medicalKit.isPresentWith(taris));

        assertEquals(1, picard.getSkillLevel(SkillName.MEDICAL));
        assertEquals(2, taris.getSkillLevel(SkillName.MEDICAL));
    }
}