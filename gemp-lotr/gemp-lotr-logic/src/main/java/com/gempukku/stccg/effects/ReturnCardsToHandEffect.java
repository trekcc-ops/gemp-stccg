package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.results.ReturnCardsToHandResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReturnCardsToHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final Filterable _filter;

    public ReturnCardsToHandEffect(PhysicalCard source, Filterable filter) {
        _source = source;
        _filter = filter;
    }

    @Override
    public String getText(DefaultGame game) {
        Collection<PhysicalCard> cards = Filters.filterActive(game, _filter);
        return "Return " + getAppendedNames(cards) + " to hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filterActive(game, _filter,
                (Filter) (game1, physicalCard) -> (_source == null || game1.getModifiersQuerying().canBeReturnedToHand(game1, physicalCard, _source))).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        GameState gameState = game.getGameState();
        Collection<PhysicalCard> cardsToReturnToHand = Filters.filterActive(game, _filter);

        // Preparation, figure out, what's going where...
        Set<PhysicalCard> discardedFromPlay = new HashSet<>();
        Set<PhysicalCard> toGoToDiscardCards = new HashSet<>();

        DiscardUtils.cardsToChangeZones(game, cardsToReturnToHand, discardedFromPlay, toGoToDiscardCards);

        Set<PhysicalCard> cardsToRemoveFromZones = new HashSet<>(toGoToDiscardCards);
        cardsToRemoveFromZones.addAll(cardsToReturnToHand);

        // Remove from their zone
        gameState.removeCardsFromZone(_source.getOwner(), cardsToRemoveFromZones);

        // Add cards to hand
        for (PhysicalCard card : cardsToReturnToHand)
            gameState.addCardToZone(game, card, Zone.HAND);

        // Add discarded to discard
        for (PhysicalCard card : toGoToDiscardCards)
            gameState.addCardToZone(game, card, Zone.DISCARD);

        if (cardsToReturnToHand.size() > 0)
            gameState.sendMessage(GameUtils.getCardLink(_source) + " returns " + getAppendedNames(cardsToReturnToHand) + " to hand");

        for (PhysicalCard discardedCard : discardedFromPlay)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
        for (PhysicalCard cardReturned : cardsToReturnToHand)
            game.getActionsEnvironment().emitEffectResult(new ReturnCardsToHandResult(cardReturned));

        return new FullEffectResult(true);
    }
}
