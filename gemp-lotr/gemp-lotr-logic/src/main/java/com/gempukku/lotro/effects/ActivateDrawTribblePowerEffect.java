package com.gempukku.lotro.effects;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

public class ActivateDrawTribblePowerEffect extends ActivateTribblePowerEffect {

    String _drawingPlayer;
    public ActivateDrawTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source,
                                          DefaultActionContext actionContext) {
        super(action, source, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
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
        return new FullEffectResult(true);
    }

    private void playerChosen(String playerId, DefaultGame game) {
        _drawingPlayer = playerId;
        new DrawCardsEffect(_action, playerId, 1).playEffect(game);
    }
}