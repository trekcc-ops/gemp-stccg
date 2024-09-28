package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.ScorePointsEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ActivateLaughterTribblePowerEffect extends ActivateTribblePowerEffect {
    private String _discardingPlayer;
    public ActivateLaughterTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        // There must be at least two players with cards in their hands
        int playersWithHands = 0;
        for (String player : _game.getAllPlayerIds()) {
            if (!_game.getGameState().getHand(player).isEmpty())
                playersWithHands++;
        }
        return playersWithHands >= 2;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (!isPlayableInFull())
            return new FullEffectResult(false);
        else {
            List<String> players = Arrays.asList(_game.getAllPlayerIds());
            players.removeIf(player -> _game.getGameState().getHand(player).isEmpty());
            _game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision("Choose a player to discard a card", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            firstPlayerChosen(players, result, _game);
                        }
                    });
            _game.getActionsEnvironment().emitEffectResult(_result);
            return new FullEffectResult(true);
        }
    }

    private void firstPlayerChosen(List<String> allPlayers, String chosenPlayer, TribblesGame game) {
        _discardingPlayer = chosenPlayer;
        List<String> newSelectablePlayers = new ArrayList<>(allPlayers);
        newSelectablePlayers.remove(chosenPlayer);
        if (newSelectablePlayers.size() == 1)
            secondPlayerChosen(Iterables.getOnlyElement(newSelectablePlayers), game);
        else {
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(
                            "Choose a player to place a card from hand on the bottom of their deck", newSelectablePlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            secondPlayerChosen(result, game);
                        }
                    });
        }
    }

    private void secondPlayerChosen(String secondPlayerChosen, TribblesGame game) {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(_game, _action, _discardingPlayer,false,1));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                game, _action, secondPlayerChosen, 1, false, Filters.any));
        if (!(Objects.equals(_discardingPlayer, _activatingPlayer) ||
                Objects.equals(secondPlayerChosen, _activatingPlayer)))
            subAction.appendEffect(new ScorePointsEffect(game, _source, _activatingPlayer, 25000));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}