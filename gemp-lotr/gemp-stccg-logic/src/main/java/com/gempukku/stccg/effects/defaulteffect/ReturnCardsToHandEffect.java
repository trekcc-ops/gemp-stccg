package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.utils.DiscardUtils;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.results.ReturnCardsToHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReturnCardsToHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final Filterable _filter;
    private final DefaultGame _game;

    public ReturnCardsToHandEffect(DefaultGame game, PhysicalCard source, Filterable filter) {
        _source = source;
        _filter = filter;
        _game = game;
    }

    @Override
    public String getText() {
        Collection<PhysicalCard> cards = Filters.filterActive(_game, _filter);
        return "Return " + GameUtils.getAppendedNames(cards) + " to hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.filterActive(_game, _filter,
                (Filter) (game1, physicalCard) -> (_source == null || game1.getModifiersQuerying().canBeReturnedToHand(game1, physicalCard, _source))).size() > 0;
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
        gameState.removeCardsFromZone(_source.getOwner(), cardsToRemoveFromZones);

        // Add cards to hand
        for (PhysicalCard card : cardsToReturnToHand)
            gameState.addCardToZone(_game, card, Zone.HAND);

        // Add discarded to discard
        for (PhysicalCard card : toGoToDiscardCards)
            gameState.addCardToZone(_game, card, Zone.DISCARD);

        if (cardsToReturnToHand.size() > 0)
            gameState.sendMessage(GameUtils.getCardLink(_source) + " returns " + GameUtils.getAppendedNames(cardsToReturnToHand) + " to hand");

        for (PhysicalCard discardedCard : discardedFromPlay)
            _game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
        for (PhysicalCard cardReturned : cardsToReturnToHand)
            _game.getActionsEnvironment().emitEffectResult(new ReturnCardsToHandResult(cardReturned));

        return new FullEffectResult(true);
    }
}
