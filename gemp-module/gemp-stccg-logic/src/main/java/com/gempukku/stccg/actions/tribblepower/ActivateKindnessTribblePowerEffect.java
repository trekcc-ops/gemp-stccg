package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.actions.choose.PutCardsFromHandBeneathDrawDeckInChosenOrderEffect;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ActivateKindnessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKindnessTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (getGame().getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new DrawCardsEffect(getGame(), _action, _activatingPlayer, 1));
            // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : getGame().getPlayerIds()) {
            if (getGame().getGameState().getHand(player).size() >= 4) {
                subAction.appendEffect(
                        new ChooseCardsFromZoneEffect(getGame(), Zone.HAND, player, 1, 1) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                        game.getGameState().removeCardsFromZone(player, selectedCards);
                        for (PhysicalCard card : selectedCards) {
                            game.sendMessage(
                                    player + " puts " + card.getCardLink() + " from hand on bottom of their play pile");
                            game.getGameState().addCardToZone(card, Zone.PLAY_PILE, false);
                        }
                    }
                });
            }
        }
        Effect effect = new PutCardsFromHandBeneathDrawDeckInChosenOrderEffect(
                _action, _activatingPlayer, false, Filters.any);
        subAction.appendEffect(effect);
        return addActionAndReturnResult(getGame(), subAction);
    }
}