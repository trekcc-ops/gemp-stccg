package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.TribblesGame;

import java.util.ArrayList;
import java.util.List;

public class ActivateRecycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateRecycleTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // Choose a player to shuffle his or her discard pile into his or her draw deck
        List<String> playersWithCards = new ArrayList<>();
        for (String player : _game.getAllPlayerIds()) {
            if (!_game.getGameState().getDiscard(player).isEmpty())
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0], _game);
        else
            _game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision("Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _game);
                        }
                    });
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        new ShuffleCardsIntoDrawDeckEffect(game,
                _source, Zone.DISCARD, chosenPlayer, game.getGameState().getDiscard(chosenPlayer)).playEffect();
    }

}