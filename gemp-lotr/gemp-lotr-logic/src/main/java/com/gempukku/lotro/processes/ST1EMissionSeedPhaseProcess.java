package com.gempukku.lotro.processes;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Quadrant;
import com.gempukku.lotro.game.ST1EGame;
import com.gempukku.lotro.gamestate.ST1EGameState;

public class ST1EMissionSeedPhaseProcess implements GameProcess<ST1EGame> {
    private String _firstPlayer;

    public ST1EMissionSeedPhaseProcess(String playerId) {
        _firstPlayer = playerId;
        // TODO: Build code for the doorway phase
    }

    @Override
    public void process(ST1EGame game) {
        ST1EGameState gameState = game.getGameState();
        // Both players shuffle their six missions and place them in a pile face-down.
        game.getPlayers().forEach(gameState::shuffleDeck);

        String currentPlayer = _firstPlayer;

        /* The starting player draws the top mission from his or her pile and places it face up on the table,
            beginning the first spaceline. */
        LotroPhysicalCard topMission = game.getGameState().getMissionPile(currentPlayer).get(0);
        Quadrant quadrant = topMission.getBlueprint().getQuadrant();

        if (gameState.spacelineExists(quadrant)) {
            gameState.createNewSpaceline(quadrant);
            gameState.addToSpaceline(topMission, quadrant, 0);
        }

                // Code below borrowed from other process
/*        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);

        if (playableActions.size() == 0 && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId);
        } else {
            String userMessage;
            if (playableActions.size() == 0) {
                userMessage = "No Tribbles can be played. Click 'Pass' to draw a card.";
            } else {
                userMessage = "Select Tribble to play or click 'Pass' to draw a card.";
            }
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(game, 1, userMessage, playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new TribblesEndOfTurnGameProcess();
                                game.getActionsEnvironment().addActionToStack(action);
                            } else
                                _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId);
                        }
                    });
        }*/


        // The second player then draws and places his or her first mission.
        // A mission can be placed on either end of the appropriate spaceline.
        // If it is the first mission in the quadrant, it is placed on a new spaceline, separate from the others.
        /* Cards that specify they are INSERTED into the spaceline may be placed anywhere in their native quadrant,
            including between two missions already seeded. */
        // This continues until both players are finished.

        // Cards that specify they are part of a region must be next to each other, forming a single, contiguous region within the quadrant.
        // The first location in a region is placed normally.
        // Subsequent locations within that region may be inserted into the spaceline at either end of the region.

        // If two players seed the same location in the same quadrant, it becomes a shared mission.
        // The first version to appear is seeded normally, but the second version is placed on top of the original, wherever it is on the spaceline, leaving half of the original exposed.
        // The two missions form only one location and may be completed only once.
        // Each player uses their own mission card for gameplay purposes; players may not use the "opponent's end" of their opponent's mission card at a shared mission.

        // Missions with the Universal symbol may seed multiple times as multiple locations.
    }

    @Override
    public GameProcess getNextProcess() { return new ST1EMissionSeedPhaseProcess(_firstPlayer);
    }
}