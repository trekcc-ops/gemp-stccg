package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_060_MedicalKit_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Medical Kit

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void medicalKitTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        ST1EGameState gameState = _game.getGameState();

        MissionCard mission = (MissionCard) newCardForGame("101_174", P1);
        EquipmentCard medicalKit = (EquipmentCard) newCardForGame("101_060", P1);
        PersonnelCard picard = (PersonnelCard) newCardForGame("101_215", P1);
        PersonnelCard taris = (PersonnelCard) newCardForGame("105_085", P1);

        // Federation Outpost
        FacilityCard outpost = (FacilityCard) newCardForGame("101_104", P1);

        assertFalse(outpost.isInPlay());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Taris", taris.getTitle());
        assertEquals("Medical Kit", medicalKit.getTitle());
        assertNotNull(mission);

        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission);

        assertTrue(outpost.isInPlay());
        reportCardsToFacility(List.of(picard, taris), outpost);
        assertEquals(0, picard.getSkillLevel(_game, SkillName.MEDICAL));
        assertEquals(1, taris.getSkillLevel(_game, SkillName.MEDICAL));

        reportCardToFacility(medicalKit, outpost);
        assertTrue(gameState.cardsArePresentWithEachOther(medicalKit, picard, taris));

        assertEquals(1, picard.getSkillLevel(_game, SkillName.MEDICAL));
        assertEquals(2, taris.getSkillLevel(_game, SkillName.MEDICAL));
    }
}