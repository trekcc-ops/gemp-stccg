package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class DoorwaySeedPhaseProcess extends SimultaneousGameProcess {

    public DoorwaySeedPhaseProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }

    public DoorwaySeedPhaseProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    public void process() {
        Iterator<String> playerIterator = _playersParticipating.iterator();
        while (playerIterator.hasNext()) {
            String playerId = playerIterator.next();
            final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(playerId);

            if (playableActions.isEmpty()) {
                playerIterator.remove();
            } else {
                String message = "Play " + _game.getGameState().getCurrentPhase() +  " action or Pass";
                _game.getUserFeedback().sendAwaitingDecision(
                        new CardActionSelectionDecision(_game.getPlayer(playerId), message, playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else
                                    playerIterator.remove();
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        if (_playersParticipating.isEmpty()) {
            ST1EGameState _gameState = _game.getGameState();
            _gameState.setCurrentPhase(Phase.SEED_MISSION);
            for (String player : _game.getPlayerIds()) {
                List<PhysicalCard> missionSeeds = new LinkedList<>(_gameState.getZoneCards(player, Zone.MISSIONS_PILE));
                if (!_game.getFormat().isNoShuffle())
                    Collections.shuffle(missionSeeds);
                for (PhysicalCard card : missionSeeds) {
                    _gameState.removeCardsFromZone(player, Collections.singleton(card));
                    _gameState.addCardToZone(card, Zone.HAND);
                }
            }
            return new ST1EMissionSeedPhaseProcess(_game);
        }
        else return new DoorwaySeedPhaseProcess(_playersParticipating, _game);
    }
}