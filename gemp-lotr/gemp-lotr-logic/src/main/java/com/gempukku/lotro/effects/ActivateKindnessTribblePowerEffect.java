package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.effects.choose.ChooseCardsFromHandEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;

public class ActivateKindnessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateKindnessTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return (game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new DrawCardsEffect(_action, _activatingPlayer, 1));
            // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : game.getPlayers()) {
            if (game.getGameState().getHand(player).size() >= 4) {
                subAction.appendEffect(new ChooseCardsFromHandEffect(player, 1, 1) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<LotroPhysicalCard> selectedCards) {
                        game.getGameState().removeCardsFromZone(player, selectedCards);
                        for (LotroPhysicalCard card : selectedCards) {
                            game.getGameState().sendMessage(player + " puts " + GameUtils.getCardLink(card) + " from hand on bottom of their play pile");
                            game.getGameState().addCardToZone(null, card, Zone.PLAY_PILE, false);
                        }
                    }
                });
            }
        }
        subAction.appendEffect(new PutCardsFromHandBeneathDrawDeckEffect(_action, _activatingPlayer, false, Filters.any));
        return addActionAndReturnResult(game, subAction);
    }
}