package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
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

        _game.addCardToGame("101_174", _cardLibrary, P1);
        _game.addCardToGame("101_065", _cardLibrary, P1);
        _game.addCardToGame("101_212", _cardLibrary, P1);
        _game.addCardToGame("172_031", _cardLibrary, P1);
        _game.addCardToGame("101_205", _cardLibrary, P1);

        MissionCard mission = null;
        PhysicalReportableCard1E tricorder = null;
        PersonnelCard geordi = null;
        PersonnelCard tamal = null;
        PersonnelCard deanna = null;

        for (PhysicalCard card : gameState.getAllCardsInGame()) {
            switch(card.getBlueprintId()) {
                case "101_174":
                    mission = (MissionCard) card;
                    break;
                case "101_065":
                    tricorder = (PhysicalReportableCard1E) card;
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
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_104"));

        assertFalse(outpost.isInPlay());
        assertEquals("Geordi La Forge", geordi.getTitle());
        assertEquals("Tamal", tamal.getTitle());
        assertEquals("Tricorder", tricorder.getTitle());
        assertEquals("Deanna Troi", deanna.getTitle());

        _game.getGameState().addMissionLocationToSpaceline(mission, 0);
        _game.getGameState().seedFacilityAtLocation(outpost, mission.getLocationDeprecatedOnlyUseForTests());

        assertTrue(outpost.isInPlay());

        geordi.reportToFacility(outpost);
        tamal.reportToFacility(outpost);
        deanna.reportToFacility(outpost);

        assertFalse(geordi.hasSkill(SkillName.SCIENCE));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE));
        assertEquals(0, geordi.getSkillLevel(SkillName.SCIENCE));
        assertEquals(1, tamal.getSkillLevel(SkillName.SCIENCE));

        tricorder.reportToFacility(outpost);
        assertTrue(tricorder.isPresentWith(geordi));
        assertTrue(tricorder.isPresentWith(tamal));
        assertTrue(tricorder.isPresentWith(deanna));

        assertTrue(geordi.hasSkill(SkillName.SCIENCE));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE));
        assertEquals(1, geordi.getSkillLevel(SkillName.SCIENCE));
        assertEquals(2, tamal.getSkillLevel(SkillName.SCIENCE));
    }
}