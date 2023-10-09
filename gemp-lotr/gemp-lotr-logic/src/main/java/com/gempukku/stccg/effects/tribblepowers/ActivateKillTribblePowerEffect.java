package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

public class ActivateKillTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKillTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        // Choose a player...
        String[] players = GameUtils.getAllPlayers(game);
        if (players.length == 1)
            playerChosen(players[0], game);
        else
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1,
                            "Choose a player to shuffle his or her discard pile into his or her draw deck", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, game);
                        }
                    });
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // ... to discard the top tribble of his or her play pile
        new DiscardCardsFromEndOfCardPileEffect(_source, Zone.PLAY_PILE, EndOfPile.TOP, chosenPlayer).playEffect(game);
    }
}