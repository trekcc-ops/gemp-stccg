package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.decisions.CardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;
import java.util.Set;

public class AllPlayersDiscardFromHandEffect extends AbstractEffect<DefaultGame> {
    private final CostToEffectAction _action;
    private final PhysicalCard _source;
    private final boolean _allPlayersMustBeAble;
    private final boolean _forced;

    public AllPlayersDiscardFromHandEffect(CostToEffectAction action, PhysicalCard source,
                                           boolean allPlayersMustBeAble, boolean forced) {
        _action = action;
        _source = source;
        _allPlayersMustBeAble = allPlayersMustBeAble;
        _forced = forced;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        boolean temp = false;
        for (String player : game.getPlayers()) {
            if ((game.getGameState().getHand(player).size() == 0) && (_allPlayersMustBeAble))
                temp = false;
            else if ((game.getGameState().getHand(player).size() > 0) && (!_allPlayersMustBeAble))
                temp = true;
        }
        return temp;
    }
    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {

            for (String player : GameUtils.getAllPlayers(game)) {
                Collection<PhysicalCard> hand = Filters.filter(game.getGameState().getHand(player), game, Filters.any);
                if (hand.size() == 1) {
                    new DiscardCardsFromZoneEffect(_action.getActionSource(), Zone.HAND, player, hand, _forced).playEffect(game);
                } else {
                    game.getUserFeedback().sendAwaitingDecision(player,
                            new CardsSelectionDecision(1, "Choose a card to discard", hand, 1, 1) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                    new DiscardCardsFromZoneEffect(_action.getActionSource(), Zone.HAND, player, cards, _forced).playEffect(game);
                                }
                            });
                }

            }
            return new FullEffectResult(true);
        }
        else
            return new FullEffectResult(false);
    }
}