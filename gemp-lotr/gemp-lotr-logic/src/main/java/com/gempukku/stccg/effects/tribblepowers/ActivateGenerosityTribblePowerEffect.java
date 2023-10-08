package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivateGenerosityTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateGenerosityTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        // You and one other player (your choice) each score 25,000 points.
        List<String> opponents = new ArrayList<>();
        for (String player : GameUtils.getAllPlayers(game)) {
            if (!Objects.equals(player, _activatingPlayer))
                opponents.add(player);
        }
        String[] opponentsArray = opponents.toArray(new String[0]);
        if (opponentsArray.length == 1)
            playerChosen(opponentsArray[0], game);
        else
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player to score 25,000 points", opponentsArray) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, game);
                        }
                    });
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // You and one other player (your choice) each score 25,000 points.
        game.getGameState().addToPlayerScore(_activatingPlayer, 25000);
        game.getGameState().addToPlayerScore(chosenPlayer, 25000);

        // Draw a card.
        new DrawCardsEffect(_action, _activatingPlayer, 1).playEffect(game);
    }
}