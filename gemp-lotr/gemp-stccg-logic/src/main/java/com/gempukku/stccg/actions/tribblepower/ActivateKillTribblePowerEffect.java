package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateKillTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKillTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // Choose a player...
        String[] players = _game.getAllPlayerIds();
        if (players.length == 1)
            playerChosen(players[0], _game);
        else
            _game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(
                            "Choose a player to shuffle his or her discard pile into his or her draw deck", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _game);
                        }
                    });
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // ... to discard the top tribble of his or her play pile
        new DiscardCardsFromEndOfCardPileEffect(game, _source, Zone.PLAY_PILE, EndOfPile.TOP, chosenPlayer).playEffect();
    }
}