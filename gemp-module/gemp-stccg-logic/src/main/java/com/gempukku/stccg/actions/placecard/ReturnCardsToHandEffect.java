package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.discard.DiscardUtils;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReturnCardsToHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final Filterable _filter;
    private final DefaultGame _game;

    public ReturnCardsToHandEffect(DefaultGame game, PhysicalCard source, Filterable filter) {
        super(source);
        _source = source;
        _filter = filter;
        _game = game;
    }

    @Override
    public String getText() {
        Collection<PhysicalCard> cards = Filters.filterActive(_game, _filter);
        return "Return " + TextUtils.concatenateStrings(cards.stream().map(PhysicalCard::getCardLink)) + " to hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return !Filters.filterActive(_game, _filter,
                (Filter) (game1, physicalCard) -> (_source == null || game1.getModifiersQuerying().canBeReturnedToHand(physicalCard, _source))).isEmpty();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        GameState gameState = _game.getGameState();
        Collection<PhysicalCard> cardsToReturnToHand = Filters.filterActive(_game, _filter);

        // Preparation, figure out, what's going where...
        Set<PhysicalCard> discardedFromPlay = new HashSet<>();
        Set<PhysicalCard> toGoToDiscardCards = new HashSet<>();

        DiscardUtils.cardsToChangeZones(_game, cardsToReturnToHand, discardedFromPlay, toGoToDiscardCards);

        Set<PhysicalCard> cardsToRemoveFromZones = new HashSet<>(toGoToDiscardCards);
        cardsToRemoveFromZones.addAll(cardsToReturnToHand);

        // Remove from their zone
        gameState.removeCardsFromZone(_source.getOwnerName(), cardsToRemoveFromZones);

        // Add cards to hand
        for (PhysicalCard card : cardsToReturnToHand)
            gameState.addCardToZone(card, Zone.HAND);

        // Add discarded to discard
        for (PhysicalCard card : toGoToDiscardCards)
            gameState.addCardToZone(card, Zone.DISCARD);

        if (!cardsToReturnToHand.isEmpty())
            gameState.sendMessage(_source.getCardLink() + " returns " + TextUtils.getConcatenatedCardLinks(cardsToReturnToHand) + " to hand");

        for (PhysicalCard discardedCard : discardedFromPlay)
            _game.getActionsEnvironment().emitEffectResult(new DiscardCardFromPlayResult(null, discardedCard));
        for (PhysicalCard cardReturned : cardsToReturnToHand)
            _game.getActionsEnvironment().emitEffectResult(new ReturnCardsToHandResult(cardReturned));

        return new FullEffectResult(true);
    }
}