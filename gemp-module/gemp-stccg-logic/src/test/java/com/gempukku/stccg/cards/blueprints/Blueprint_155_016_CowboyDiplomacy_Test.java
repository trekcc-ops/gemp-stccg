package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_016_CowboyDiplomacy_Test extends AbstractAtTest {

    private PhysicalCard diplomacy;
    private PersonnelCard larson;

    private void initializeGame(boolean diplomacyAtOpponentsMission)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_154", "Excavation", P2);
        builder.addFacility("101_104", P1, mission);
        MissionCard homeworld = builder.addMission("117_046", "Deliver Message", P1);
        if (diplomacyAtOpponentsMission) {
            builder.addCardOnPlanetSurface(
                    "101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        } else {
            builder.addCardOnPlanetSurface(
                    "101_215", "Jean-Luc Picard", P1, homeworld, PersonnelCard.class);
        }
        diplomacy = builder.addCardInHand("155_016", "Cowboy Diplomacy", P1);
        diplomacy = builder.addCardInHand("155_016", "Cowboy Diplomacy", P1);
        larson = builder.addCardInHand("101_220", "Linda Larson", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void scorePointsTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException {
        initializeGame(true);

        // Play Cowboy Diplomacy; verify that it played for free
        playCard(P1, diplomacy);
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));

        // Verify that player scored points
        assertEquals(5, _game.getPlayer(P1).getScore());

        // Verify that Cowboy Diplomacy was placed in point area
        assertFalse(diplomacy.isInPlay());
        assertEquals(Zone.POINT_AREA, diplomacy.getZone());
        assertTrue(_game.getGameState().getCardGroup(P1, Zone.POINT_AREA).getCards().contains(diplomacy));

        // Can't play twice
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, diplomacy));
    }

    @Test
    public void playForFreeTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException {
        // Checking that Cowboy Diplomacy can still be played if it's following a normal card play
        initializeGame(true);
        playCard(P1, larson);
        playCard(P1, diplomacy);
    }

    @Test
    public void cannotPlayTest() throws InvalidGameOperationException, CardNotFoundException {
        initializeGame(false);
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, diplomacy));
    }


}