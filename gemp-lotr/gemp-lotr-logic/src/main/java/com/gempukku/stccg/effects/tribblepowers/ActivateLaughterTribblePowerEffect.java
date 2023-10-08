package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.ScorePointsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ActivateLaughterTribblePowerEffect extends ActivateTribblePowerEffect {
    private String _discardingPlayer;
    public ActivateLaughterTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        // There must be at least two players with cards in their hands
        int playersWithHands = 0;
        for (String player : GameUtils.getAllPlayers(game)) {
            if (game.getGameState().getHand(player).size() > 0)
                playersWithHands++;
        }
        return playersWithHands >= 2;
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        if (!isPlayableInFull(game))
            return new AbstractEffect.FullEffectResult(false);
        else {
            List<String> players = Arrays.asList(GameUtils.getAllPlayers(game));
            players.removeIf(player -> game.getGameState().getHand(player).size() == 0);
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player to discard a card", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            firstPlayerChosen(players, result, game);
                        }
                    });
            game.getActionsEnvironment().emitEffectResult(_result);
            return new AbstractEffect.FullEffectResult(true);
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
                    new MultipleChoiceAwaitingDecision(1,
                            "Choose a player to place a card from hand on the bottom of their deck", newSelectablePlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            secondPlayerChosen(result, game);
                        }
                    });
        }
    }

    private void secondPlayerChosen(String secondPlayerChosen, TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(_action, _discardingPlayer,false,1));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                _action, secondPlayerChosen, 1, false, Filters.any));
        if (!(Objects.equals(_discardingPlayer, _activatingPlayer) ||
                Objects.equals(secondPlayerChosen, _activatingPlayer)))
            subAction.appendEffect(new ScorePointsEffect(_source, _activatingPlayer, 25000));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}