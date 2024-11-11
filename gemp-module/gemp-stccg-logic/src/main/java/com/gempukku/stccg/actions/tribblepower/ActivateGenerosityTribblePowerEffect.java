package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.TribblesGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivateGenerosityTribblePowerEffect extends ActivateTribblePowerEffect {

    private final static int BONUS_POINTS = 25000;
    public ActivateGenerosityTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // You and one other player (your choice) each score 25,000 points.
        List<String> opponents = new ArrayList<>();
        for (String player : getGame().getAllPlayerIds()) {
            if (!Objects.equals(player, _activatingPlayer))
                opponents.add(player);
        }
        String[] opponentsArray = opponents.toArray(new String[0]);
        if (opponentsArray.length == 1)
            playerChosen(opponentsArray[0], getGame());
        else
            getGame().getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_activatingPlayer),
                            "Choose a player to score 25,000 points", opponentsArray) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _tribblesGame);
                        }
                    });
        getGame().getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // You and one other player (your choice) each score 25,000 points.
        game.getGameState().addToPlayerScore(_activatingPlayer, BONUS_POINTS);
        game.getGameState().addToPlayerScore(chosenPlayer, BONUS_POINTS);

        // Draw a card.
        new DrawCardsEffect(game, _action, _activatingPlayer, 1).playEffect();
    }
}