package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_125_DeathYell_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private MissionCard _mission;
    private PhysicalCard _armus;
    private PhysicalCard deathYell;
    private PersonnelCard worf;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addFacility("101_104", P1, _mission); // Federation Outpost
        _armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, _mission);
        worf = builder.addCardAboardShipOrFacility("101_251", "Worf", P1, outpost, PersonnelCard.class);
        deathYell = builder.addCardInHand("101_125", "Klingon Death Yell", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void deathYellTest() throws Exception {
        initializeGame();
        assertEquals(0, _game.getPlayer(P1).getScore());

        // Beam to the planet and attempt mission
        beamCard(P1, outpost, worf, _mission);
        attemptMission(P1, _game.getGameState().getAwayTeamForCard(worf), _mission);

        // Confirm that Worf was killed by Armus
        assertEquals(Zone.DISCARD, worf.getZone());

        // Play Klingon Death Yell as response
        assertFalse(deathYell.isInPlay());
        selectAction(UseGameTextAction.class, deathYell, P1);

        Player player1 = _game.getPlayer(P1);

        assertEquals(5, _game.getPlayer(P1).getScore());
        assertFalse(deathYell.isInPlay());
        assertTrue(player1.getDiscardPile().contains(deathYell));
    }
}