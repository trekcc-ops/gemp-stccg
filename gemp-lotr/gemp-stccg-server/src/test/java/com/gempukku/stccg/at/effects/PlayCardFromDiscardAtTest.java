package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PlayCardFromDiscardAtTest extends AbstractAtTest {
    @Test
    public void cantPlayCardDiscardedWithTheyAreComing() throws Exception {
        initializeSimplestGame();

        final PhysicalCardImpl theyAreComing = createCard(P2,  "1_196");
        final PhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");

        _game.getGameState().addCardToZone(_game, theyAreComing, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        for (int i = 0; i < 3; i++) {
            final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
            _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);
        }

        skipMulligans();

        _game.getGameState().setTwilight(10);

        assertEquals(Phase.FELLOWSHIP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");

        playerDecided(P2, getCardActionId(P2, "Use They Are Coming"));

        Map<String, String[]> decisionParameters = _userFeedback.getAwaitingDecision(P2).getDecisionParameters();
        String[] cardId = decisionParameters.get("cardId");
        assertEquals(1, cardId.length);
        assertEquals(String.valueOf(goblinSneakInDiscard.getCardId()), cardId[0]);
    }

    @Test
    public void flameOfUdunCantPlayCardIfNotEnoughTwilight() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        // Fellowship phase
        playerDecided(P1, "");

        // Shadow phase
        playerDecided(P2, "");

        // End regroup phase
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");
        playerDecided(P2, "");

        final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);

        final PhysicalCardImpl balrog = createCard(P2,  "2_52");
        _game.getGameState().addCardToZone(_game, balrog, Zone.HAND);

        final PhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        _game.getGameState().setTwilight(18);

        // Move again
        playerDecided(P1, "0");

        playerDecided(P2, getCardActionId(P2, "Play Goblin Runner"));
        playerDecided(P2, getCardActionId(P2, "Play The Balrog"));
        assertNull(getCardActionId(P2, "Use The Balrog"));
    }

    @Test
    public void flameOfUdunCanPlayCardIfEnoughTwilight() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        // Fellowship phase
        playerDecided(P1, "");

        // Shadow phase
        playerDecided(P2, "");

        // End regroup phase
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");
        playerDecided(P2, "");

        final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);

        final PhysicalCardImpl balrog = createCard(P2,  "2_52");
        _game.getGameState().addCardToZone(_game, balrog, Zone.HAND);

        final PhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Move again
        playerDecided(P1, "0");

        playerDecided(P2, getCardActionId(P2, "Play Goblin Runner"));
        playerDecided(P2, getCardActionId(P2, "Play The Balrog"));
        playerDecided(P2, getCardActionId(P2, "Use The Balrog"));
        assertEquals(Zone.SHADOW_CHARACTERS, goblinSneakInDiscard.getZone());
    }

    @Test
    public void azogCantPlayTrollFromDiscardInRivendell() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl rivendell = createCard(P2, "30_51");
        rivendell.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, rivendell, Zone.ADVENTURE_PATH);

        PhysicalCardImpl azog = createCard(P2, "32_28");
        _game.getGameState().addCardToZone(_game, azog, Zone.SHADOW_CHARACTERS);

        PhysicalCardImpl watchfulOrc = createCard(P2, "30_40");
        _game.getGameState().addCardToZone(_game, watchfulOrc, Zone.SHADOW_CHARACTERS);

        PhysicalCardImpl demolitionTroll = createCard(P2, "32_31");
        _game.getGameState().addCardToZone(_game, demolitionTroll, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");
        // Shadow phase
        playerDecided(P2, getCardActionId(P2, "Use Watchful Orc"));
        // Choose Battleground
        playerDecided(P2, "0");
        playerDecided(P2, "");
        // Maneuver phase
        playerDecided(P1, "");

        assertNull(getCardActionId(P2, "Use Azog"));
    }
}
