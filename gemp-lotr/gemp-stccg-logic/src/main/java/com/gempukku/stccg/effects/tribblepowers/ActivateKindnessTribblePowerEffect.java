package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.abstractsubaction.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.effects.choose.PutCardsFromHandBeneathDrawDeckInChosenOrderEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ActivateKindnessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKindnessTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (_game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new DrawCardsEffect(_game, _action, _activatingPlayer, 1));
            // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : _game.getPlayerIds()) {
            if (_game.getGameState().getHand(player).size() >= 4) {
                subAction.appendEffect(new ChooseCardsFromZoneEffect(_game, Zone.HAND, player, 1, 1) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                        game.getGameState().removeCardsFromZone(player, selectedCards);
                        for (PhysicalCard card : selectedCards) {
                            game.getGameState().sendMessage(player + " puts " + card.getCardLink() + " from hand on bottom of their play pile");
                            game.getGameState().addCardToZone(card, Zone.PLAY_PILE, false);
                        }
                    }
                });
            }
        }
        subAction.appendEffect(new PutCardsFromHandBeneathDrawDeckInChosenOrderEffect(_game, _action, _activatingPlayer, false, Filters.any));
        return addActionAndReturnResult(_game, subAction);
    }
}