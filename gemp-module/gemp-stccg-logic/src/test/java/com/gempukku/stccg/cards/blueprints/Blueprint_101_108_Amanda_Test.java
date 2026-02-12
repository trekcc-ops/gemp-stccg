package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_108_Amanda_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard mission;
    private PhysicalCard _armus;
    private PhysicalCard deathYell;
    private PersonnelCard worf;
    private PhysicalCard amandaPlayerTwo;
    private PhysicalCard amandaPlayerOne;

    private void initializeGame(boolean playerOneHasAmanda) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        _armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, mission);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        worf = builder.addCardAboardShipOrFacility("101_251", "Worf", P1, outpost, PersonnelCard.class);
        deathYell = builder.addCardInHand("101_125", "Klingon Death Yell", P1);
        amandaPlayerTwo = builder.addCardInHand("101_108", "Amanda Rogers", P2);
        if (playerOneHasAmanda)
            amandaPlayerOne = builder.addCardInHand("101_108", "Amanda Rogers", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    private void playDeathYell() throws InvalidGameOperationException, DecisionResultInvalidException {
        // Beam Worf to the planet
        beamCard(P1, outpost, worf, mission);

        // Attempt mission
        attemptMission(P1, _game.getGameState().getAwayTeamForCard(worf), mission);

        // Confirm that Worf was killed
        assertEquals(Zone.DISCARD, worf.getZone());

        // Play Klingon Death Yell as response
        assertFalse(deathYell.isInPlay());
        selectAction(UseGameTextAction.class, deathYell, P1);
        assertTrue(deathYell.isInPlay());
    }


    @Test
    public void amandaRogersTest() throws Exception {
        initializeGame(false);
        playDeathYell();

        // P2 plays Amanda Rogers as response
        selectAction(UseGameTextAction.class, amandaPlayerTwo, P2);
        assertFalse(amandaPlayerTwo.isInPlay());
        assertFalse(deathYell.isInPlay());
    }

    @Test
    public void twoAmandasTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(true);
        playDeathYell();

        Player player1 = _game.getPlayer(P1);

        // P2 plays Amanda Rogers as response, but it doesn't immediately resolve because P1 has an Amanda too
        selectAction(UseGameTextAction.class, amandaPlayerTwo, P2);
        assertTrue(amandaPlayerTwo.isInPlay());
        assertTrue(deathYell.isInPlay());
        assertEquals(0, player1.getScore());

        // P1 plays Amanda Rogers as response
        selectAction(UseGameTextAction.class, amandaPlayerOne, P1);
        assertFalse(amandaPlayerOne.isInPlay());
        assertEquals(5, player1.getScore());
    }

    @Test
    public void wrongResponseTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(true);
        playDeathYell();

        // Try to respond with player1's Amanda Rogers (it should be P2's turn)
        boolean errorThrown = false;
        try {
            selectAction(UseGameTextAction.class, amandaPlayerOne, P1);
        } catch(DecisionResultInvalidException exp) {
            errorThrown = true;
        }
        assertTrue(errorThrown);
        assertFalse(amandaPlayerOne.isInPlay());
    }


}