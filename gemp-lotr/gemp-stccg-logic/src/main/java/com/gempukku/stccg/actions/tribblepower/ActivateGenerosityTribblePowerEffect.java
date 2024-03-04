package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import com.gempukku.stccg.game.TribblesGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivateGenerosityTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateGenerosityTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // You and one other player (your choice) each score 25,000 points.
        List<String> opponents = new ArrayList<>();
        for (String player : _game.getAllPlayerIds()) {
            if (!Objects.equals(player, _activatingPlayer))
                opponents.add(player);
        }
        String[] opponentsArray = opponents.toArray(new String[0]);
        if (opponentsArray.length == 1)
            playerChosen(opponentsArray[0], _game);
        else
            _game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision("Choose a player to score 25,000 points", opponentsArray) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _game);
                        }
                    });
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // You and one other player (your choice) each score 25,000 points.
        game.getGameState().addToPlayerScore(_activatingPlayer, 25000);
        game.getGameState().addToPlayerScore(chosenPlayer, 25000);

        // Draw a card.
        new DrawCardsEffect(game, _action, _activatingPlayer, 1).playEffect();
    }
}