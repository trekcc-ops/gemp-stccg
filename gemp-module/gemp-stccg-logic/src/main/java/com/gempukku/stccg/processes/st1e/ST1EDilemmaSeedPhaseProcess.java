package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.SeedMissionCardsAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ST1EDilemmaSeedPhaseProcess extends ST1EGameProcess {
    private final PlayerOrder _playOrder;
    private int _consecutivePasses;
    private final ST1EGameProcess _followingGameProcess;

    public ST1EDilemmaSeedPhaseProcess(int consecutivePasses, ST1EGameProcess followingGameProcess, ST1EGame game) {
        super(game);
        _followingGameProcess = followingGameProcess;
        _consecutivePasses = consecutivePasses;
        _playOrder = game.getGameState().getPlayerOrder();
    }

    @Override
    public void process() {
        String _currentPlayer = _playOrder.getCurrentPlayer();
        ST1EGameState gameState = _game.getGameState();
        List<Action> seedActions = new ArrayList<>();
        for (ST1ELocation location : gameState.getSpacelineLocations()) {
            try {
                MissionCard mission = location.getMissionForPlayer(_currentPlayer);
                Action seedCardsAction = new SeedMissionCardsAction(gameState.getPlayer(_currentPlayer), mission);
                seedActions.add(seedCardsAction);
            } catch(InvalidGameLogicException exp) { }
        }

        _game.getUserFeedback().sendAwaitingDecision(_currentPlayer,
                new CardActionSelectionDecision("Select a mission to seed cards under", seedActions) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        Action action = getSelectedAction(result);
                        if (action != null) {
                            _consecutivePasses = 0;
                            _game.getActionsEnvironment().addActionToStack(action);
                        } else {
                            _consecutivePasses++;
                        }
                    }
                });
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EStartOfFacilitySeedPhaseProcess(_game);
    }

    public void selectSeedCards(String playerId, PhysicalCard mission) {
        ST1EGameState gameState = _game.getGameState();
        List<PhysicalCard> eligibleCards = new ArrayList<>();
        for (PhysicalCard card : gameState.getHand(playerId)) {
            eligibleCards.add(card);
        }

        _game.getUserFeedback().sendAwaitingDecision(playerId,
                new ArbitraryCardsSelectionDecision(1, "Select cards to seed under " + mission.getTitle(), eligibleCards
                        , 0,
                        eligibleCards.size()) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        gameState.sendMessage(TextUtils.getConcatenatedCardLinks(selectedCards) + " were seeded");
                    }
                });
    }

}