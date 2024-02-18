package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.Set;

public class AllPlayersDiscardFromHandEffect extends DefaultEffect {
    private final CostToEffectAction _action;
    private final boolean _allPlayersMustBeAble;
    private final boolean _forced;
    private final TribblesGame _game;

    public AllPlayersDiscardFromHandEffect(TribblesGame game, CostToEffectAction action,
                                           boolean allPlayersMustBeAble, boolean forced) {
        _game = game;
        _action = action;
        _allPlayersMustBeAble = allPlayersMustBeAble;
        _forced = forced;
    }

    @Override
    public boolean isPlayableInFull() {
        boolean temp = false;
        for (String player : _game.getPlayerIds()) {
            if ((_game.getGameState().getHand(player).isEmpty()) && (_allPlayersMustBeAble))
                temp = false;
            else if ((!_game.getGameState().getHand(player).isEmpty()) && (!_allPlayersMustBeAble))
                temp = true;
        }
        return temp;
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {

            for (String player : GameUtils.getAllPlayers(_game)) {
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