package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public abstract class AbstractPreventableCardEffect extends AbstractEffect<DefaultGame> implements PreventableCardEffect {
    private final Filter _filter;
    private final Set<PhysicalCard> _preventedTargets = new HashSet<>();
    private int _requiredTargets;

    public AbstractPreventableCardEffect(PhysicalCard... cards) {
        List<PhysicalCard> affectedCards = Arrays.asList(cards);
        _requiredTargets = affectedCards.size();
        _filter = Filters.in(affectedCards);
    }

    public AbstractPreventableCardEffect(Filterable... filter) {
        _filter = Filters.and(filter);
    }

    protected abstract Filter getExtraAffectableFilter();

    protected final Collection<PhysicalCard> getAffectedCards(DefaultGame game) {
        return Filters.filterActive(game, _filter, getExtraAffectableFilter());
    }

    public final Collection<PhysicalCard> getAffectedCardsMinusPrevented(DefaultGame game) {
        Collection<PhysicalCard> affectedCards = getAffectedCards(game);
        affectedCards.removeAll(_preventedTargets);
        return affectedCards;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getAffectedCardsMinusPrevented(game).size() >= _requiredTargets;
    }

    protected abstract void playOutEffectOn(DefaultGame game, Collection<PhysicalCard> cards);

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        Collection<PhysicalCard> affectedMinusPreventedCards = getAffectedCardsMinusPrevented(game);
        playOutEffectOn(game, affectedMinusPreventedCards);
        return new FullEffectResult(affectedMinusPreventedCards.size() >= _requiredTargets);
    }

    public void preventEffect(DefaultGame game, PhysicalCard card) {
        _preventedTargets.add(card);
        _prevented = true;
    }
}
