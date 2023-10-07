package com.gempukku.lotro.effects.tribblepowers;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.EndOfPile;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.effects.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.rules.GameUtils;

public class ActivateKillTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKillTribblePowerEffect(CostToEffectAction<TribblesGame> action, PhysicalCard source) {
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