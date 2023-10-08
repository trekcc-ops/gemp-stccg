package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseCardsFromHandEffect;
import com.gempukku.stccg.effects.choose.PutCardsFromHandBeneathDrawDeckInChosenOrderEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public class ActivateKindnessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKindnessTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return (game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new DrawCardsEffect(_action, _activatingPlayer, 1));
            // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : game.getPlayers()) {
            if (game.getGameState().getHand(player).size() >= 4) {
                subAction.appendEffect(new ChooseCardsFromHandEffect(player, 1, 1) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                        game.getGameState().removeCardsFromZone(player, selectedCards);
                        for (PhysicalCard card : selectedCards) {
                            game.getGameState().sendMessage(player + " puts " + GameUtils.getCardLink(card) + " from hand on bottom of their play pile");
                            game.getGameState().addCardToZone(null, card, Zone.PLAY_PILE, false);
                        }
                    }
                });
            }
        }
        subAction.appendEffect(new PutCardsFromHandBeneathDrawDeckInChosenOrderEffect(_action, _activatingPlayer, false, Filters.any));
        return addActionAndReturnResult(game, subAction);
    }
}