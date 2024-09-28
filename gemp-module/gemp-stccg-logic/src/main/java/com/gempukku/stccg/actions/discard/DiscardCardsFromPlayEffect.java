package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.TextUtils;

import java.util.*;

public class DiscardCardsFromPlayEffect extends DefaultEffect implements PreventableCardEffect {
    private final PhysicalCard _source;
    private final Filter _filter;
    private final Set<PhysicalCard> _preventedTargets = new HashSet<>();
    private final String _performingPlayer;
    private int _requiredTargets;

    public DiscardCardsFromPlayEffect(DefaultGame game, String performingPlayer, PhysicalCard source, Filterable... filters) {
        super(game, performingPlayer);
        _filter = Filters.and(filters);
        _performingPlayer = performingPlayer;
        _source = source;
    }

    public DiscardCardsFromPlayEffect(ActionContext actionContext, String performingPlayer, Filterable... filters) {
        super(actionContext, performingPlayer);
        _filter = Filters.and(filters);
        _performingPlayer = performingPlayer;
        _source = actionContext.getSource();
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public void preventEffect(DefaultGame game, PhysicalCard card) {
        _preventedTargets.add(card);
        _prevented = true;
    }
    protected Filter getExtraAffectableFilter() {
        if (_source == null)
            return Filters.any;
        return Filters.canBeDiscarded(_performingPlayer, _source);
    }

    public String getPerformingPlayerId() {
        return _performingPlayer;
    }

    @Override
    public EffectType getType() {
        return EffectType.BEFORE_DISCARD_FROM_PLAY;
    }


    @Override
    public String getText() {
        return "Discard " + TextUtils.concatenateStrings(
                getAffectedCardsMinusPrevented().stream().map(PhysicalCard::getFullName));
    }

    protected void forEachDiscardedByEffectCallback(Collection<PhysicalCard> discardedCards) {

    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        Collection<PhysicalCard> affectedMinusPreventedCards = getAffectedCardsMinusPrevented();
        playOutEffectOn(_game, affectedMinusPreventedCards);
        return new FullEffectResult(affectedMinusPreventedCards.size() >= _requiredTargets);
    }

    @Override
    public boolean isPlayableInFull() {
        return getAffectedCardsMinusPrevented().size() >= _requiredTargets;
    }

    protected final Collection<PhysicalCard> getAffectedCards() {
        return Filters.filterActive(_game, _filter, getExtraAffectableFilter());
    }

    public final Collection<PhysicalCard> getAffectedCardsMinusPrevented() {
        Collection<PhysicalCard> affectedCards = getAffectedCards();
        affectedCards.removeAll(_preventedTargets);
        return affectedCards;
    }

    protected void playOutEffectOn(DefaultGame game, Collection<PhysicalCard> cards) {
        Set<PhysicalCard> discardedCards = new HashSet<>();

        Set<PhysicalCard> toMoveFromZoneToDiscard = new HashSet<>();

        GameState gameState = game.getGameState();

        DiscardUtils.cardsToChangeZones(game, cards, discardedCards, toMoveFromZoneToDiscard);

        discardedCards.addAll(cards);
        toMoveFromZoneToDiscard.addAll(cards);

        gameState.removeCardsFromZone(_performingPlayer, toMoveFromZoneToDiscard);

        for (PhysicalCard card : toMoveFromZoneToDiscard)
            gameState.addCardToZone(card, Zone.DISCARD);

        if (_source != null && !discardedCards.isEmpty())
            game.sendMessage(_performingPlayer + " discards " + TextUtils.concatenateStrings(cards.stream().map(PhysicalCard::getCardLink)) + " from play using " + _source.getCardLink());

        for (PhysicalCard discardedCard : discardedCards)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(_source, getPerformingPlayerId(), discardedCard));

        forEachDiscardedByEffectCallback(cards);
    }
}
