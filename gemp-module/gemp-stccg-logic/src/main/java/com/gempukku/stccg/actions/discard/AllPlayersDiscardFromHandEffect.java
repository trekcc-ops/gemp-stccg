package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.Set;

public class AllPlayersDiscardFromHandEffect extends DefaultEffect {
    private final CostToEffectAction _action;
    private final boolean _allPlayersMustBeAble;
    private final boolean _forced;

    public AllPlayersDiscardFromHandEffect(CostToEffectAction action, boolean allPlayersMustBeAble, boolean forced) {
        super(action);
        _action = action;
        _allPlayersMustBeAble = allPlayersMustBeAble;
        _forced = forced;
    }

    @Override
    public boolean isPlayableInFull() {
        return _allPlayersMustBeAble ?
                _game.getPlayerIds().stream().noneMatch(player -> _game.getGameState().getHand(player).isEmpty()) :
                _game.getPlayerIds().stream().anyMatch(player -> !_game.getGameState().getHand(player).isEmpty());
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {

            for (String player : _game.getAllPlayerIds()) {
                Collection<PhysicalCard> hand = Filters.filter(_game.getGameState().getHand(player), _game, Filters.any);
                if (hand.size() == 1) {
                    new DiscardCardsFromZoneEffect(_game, _action.getActionSource(), Zone.HAND, player, hand, _forced).playEffect();
                } else {
                    _game.getUserFeedback().sendAwaitingDecision(player,
                            new CardsSelectionDecision(1, "Choose a card to discard", hand, 1, 1) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                    new DiscardCardsFromZoneEffect(_game, _action.getActionSource(), Zone.HAND, player, cards, _forced).playEffect();
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