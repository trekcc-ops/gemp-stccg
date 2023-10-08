package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

public class ActivateDrawTribblePowerEffect extends ActivateTribblePowerEffect {

    String _drawingPlayer;
    public ActivateDrawTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        String[] players = GameUtils.getAllPlayers(game);
        if (players.length == 1)
            playerChosen(players[0], game);
        else
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, game);
                        }
                    });
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }

    private void playerChosen(String playerId, TribblesGame game) {
        _drawingPlayer = playerId;
        new DrawCardsEffect(_action, playerId, 1).playEffect(game);
    }
}