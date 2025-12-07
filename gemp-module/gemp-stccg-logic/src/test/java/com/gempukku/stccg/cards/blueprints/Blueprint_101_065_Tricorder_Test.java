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

public class Blueprint_101_065_Tricorder_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Tricorder

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void tricorderTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        ST1EGameState gameState = _game.getGameState();

        _game.addCardToGame("101_174", P1);
        _game.addCardToGame("101_065", P1);
        _game.addCardToGame("101_212", P1);
        _game.addCardToGame("172_031", P1);
        _game.addCardToGame("101_205", P1);

        MissionCard mission = null;
        EquipmentCard tricorder = null;
        PersonnelCard geordi = null;
        PersonnelCard tamal = null;
        PersonnelCard deanna = null;

        for (PhysicalCard card : gameState.getAllCardsInGame()) {
            switch(card.getBlueprintId()) {
                case "101_174":
                    mission = (MissionCard) card;
                    break;
                case "101_065":
                    tricorder = (EquipmentCard) card;
                    break;
                case "101_212":
                    geordi = (PersonnelCard) card;
                    break;
                case "172_031":
                    tamal = (PersonnelCard) card;
                    break;
                case "101_205":
                    deanna = (PersonnelCard) card;
                    break;
                default:
            }
        }

        // Federation Outpost
        FacilityCard outpost = (FacilityCard) newCardForGame("101_104", P1);

        assertFalse(outpost.isInPlay());
        assertEquals("Geordi La Forge", geordi.getTitle());
        assertEquals("Tamal", tamal.getTitle());
        assertEquals("Tricorder", tricorder.getTitle());
        assertEquals("Deanna Troi", deanna.getTitle());
        assertNotNull(mission);

        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission);

        assertTrue(outpost.isInPlay());
        reportCardsToFacility(outpost, geordi, tamal, deanna);

        assertFalse(geordi.hasSkill(SkillName.SCIENCE, _game));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE, _game));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.SCIENCE));
        assertEquals(1, tamal.getSkillLevel(_game, SkillName.SCIENCE));

        reportCardToFacility(tricorder, outpost);
        assertTrue(gameState.cardsArePresentWithEachOther(tricorder, geordi, tamal, deanna));

        assertTrue(geordi.hasSkill(SkillName.SCIENCE, _game));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE, _game));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.SCIENCE));
        assertEquals(2, tamal.getSkillLevel(_game, SkillName.SCIENCE));
    }
}