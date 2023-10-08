package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivateRecycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateRecycleTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
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
        return new AbstractEffect.FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        new ShuffleCardsIntoDrawDeckEffect(
                _source, Zone.DISCARD, chosenPlayer, game.getGameState().getDiscard(chosenPlayer)).playEffect(game);
    }

}