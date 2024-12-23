package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.TribblesGame;

import java.util.ArrayList;
import java.util.List;

public class ActivateRecycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateRecycleTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // Choose a player to shuffle his or her discard pile into his or her draw deck
        List<String> playersWithCards = new ArrayList<>();
        for (String player : getGame().getAllPlayerIds()) {
            if (!getGame().getGameState().getDiscard(player).isEmpty())
                playersWithCards.add(player);
        }
        if (playersWithCards.size() == 1)
            playerChosen(playersWithCards.getFirst(), getGame());
        else
            getGame().getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_activatingPlayer), "Choose a player",
                            playersWithCards) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _tribblesGame);
                        }
                    });
        getGame().getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        new ShuffleCardsIntoDrawDeckEffect(game,
                _source, Zone.DISCARD, chosenPlayer, game.getGameState().getDiscard(chosenPlayer)).playEffect();
    }

}