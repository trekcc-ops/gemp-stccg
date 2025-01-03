package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

public class ActivateKillTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKillTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // Choose a player...
        String[] players = getGame().getAllPlayerIds();
        if (players.length == 1)
            playerChosen(players[0]);
        else
            getGame().getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_activatingPlayer),
                            "Choose a player to shuffle his or her discard pile into his or her draw deck",
                            players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
        getGame().getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer) {
        // ... to discard the top tribble of his or her play pile
        new DiscardCardsFromEndOfCardPileEffect(Zone.PLAY_PILE, EndOfPile.TOP, chosenPlayer, _context).playEffect();
    }
}