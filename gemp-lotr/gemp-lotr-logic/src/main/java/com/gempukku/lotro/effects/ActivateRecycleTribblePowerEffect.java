package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivateRecycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateRecycleTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        // Choose a player to shuffle his or her discard pile into his or her draw deck
        List<String> playersWithCards = new ArrayList<>();
        for (String player : GameUtils.getAllPlayers(game)) {
            if (game.getGameState().getDiscard(player).size() > 0)
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0], game);
        else
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, game);
                        }
                    });
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        new ShuffleCardsFromDiscardIntoDeckEffect(
                _source, chosenPlayer, game.getGameState().getDiscard(chosenPlayer)).playEffect(game);
    }

}