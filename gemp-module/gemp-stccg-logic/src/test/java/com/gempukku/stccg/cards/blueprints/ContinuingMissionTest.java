package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContinuingMissionTest extends AbstractAtTest {

    // Test of Continuing Mission
    @Test
    public void introTwoPlayerGameWithSeedCardsTest() throws DecisionResultInvalidException, InvalidGameOperationException {
        initializeGameWithAttentionAllHands();
        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) skipDilemma();
        autoSeedFacility();

        // Should be 12 missions, 2 facilities, 2 Continuing Mission, 2 Attention All Hands
        assertEquals(18, _game.getGameState().getAllCardsInPlay().size());
    }

    @Test
    public void makingDecisionsTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException {
        initializeGameWithAttentionAllHands();
        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) skipDilemma();
        Player currPlayer = _game.getCurrentPlayer();

        PhysicalCard continuingMission = getCardInGame("Continuing Mission", currPlayer, Zone.SEED_DECK);

        assertFalse(continuingMission.isInPlay());
        seedCardIfAllowed(continuingMission);
        assertTrue(continuingMission.isInPlay());
    }

    private PhysicalCard getCardInGame(String cardTitle, Player cardOwner, Zone zone) {
        Collection<PhysicalCard> possibleCandidates = Filters.filter(
                _game.getGameState().getZoneCards(cardOwner, zone),
                _game,
                Filters.name(cardTitle)
        );
        assertEquals(1, possibleCandidates.size());
        return Iterables.getOnlyElement(possibleCandidates);
    }

    private void seedCardIfAllowed(PhysicalCard card) throws DecisionResultInvalidException, InvalidGameOperationException {
        String ownerId = card.getOwnerName();
        // Assumes there is only one valid location for the card to seed at
        assertNotNull(_game.getAwaitingDecision(ownerId));
        assertInstanceOf(ActionSelectionDecision.class, _game.getAwaitingDecision(ownerId));
        ActionSelectionDecision decision =
                (ActionSelectionDecision) _game.getAwaitingDecision(ownerId);
        List<? extends Action> possibleActions = decision.getActions();
        int decisionIndex = -1;
        SeedCardAction seedAction = null;
        for (Action action : possibleActions) {
            if (action instanceof SeedCardAction seedThisAction && seedThisAction.getCardEnteringPlay() == card) {
                decisionIndex = possibleActions.indexOf(action);
                seedAction = seedThisAction;
            }
        }
        assertNotEquals(-1, decisionIndex);
        if (seedAction != null) {
            playerDecided(ownerId, String.valueOf(seedAction.getActionId()));
        }
    }
}