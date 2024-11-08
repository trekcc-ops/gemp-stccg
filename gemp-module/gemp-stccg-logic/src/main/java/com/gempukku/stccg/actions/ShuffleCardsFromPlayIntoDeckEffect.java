package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.discard.DiscardUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromPlayIntoDeckEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerDeck;
    private final Collection<? extends PhysicalCard> _cards;
    private final DefaultGame _game;

    public ShuffleCardsFromPlayIntoDeckEffect(ActionContext actionContext, String playerDeck, Collection<? extends PhysicalCard> cards) {
        super(actionContext);
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _playerDeck = playerDeck;
        _cards = cards;
    }

    @Override
    public boolean isPlayableInFull() {
        for (PhysicalCard card : _cards) {
            if (!card.getZone().isInPlay())
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        Set<PhysicalCard> goingToDiscard = new HashSet<>();
        Set<PhysicalCard> discardedFromPlay = new HashSet<>();
        Set<PhysicalCard> toShuffleIn = new HashSet<>();

        for (PhysicalCard card : _cards) {
            if (card.getZone().isInPlay()) {
                toShuffleIn.add(card);
            }
        }

        if (!toShuffleIn.isEmpty()) {
            DiscardUtils.cardsToChangeZones(_game, toShuffleIn, discardedFromPlay, goingToDiscard);

            Set<PhysicalCard> removeFromPlay = new HashSet<>(goingToDiscard);
            removeFromPlay.addAll(toShuffleIn);

            _game.getGameState().removeCardsFromZone(_source.getOwnerName(), removeFromPlay);

            _game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _playerDeck);

            for (PhysicalCard physicalCard : goingToDiscard)
                _game.getGameState().addCardToZone(physicalCard, Zone.DISCARD);

            for (PhysicalCard physicalCard : discardedFromPlay)
                _game.getActionsEnvironment().emitEffectResult(new DiscardCardFromPlayResult(null, physicalCard));

            _game.sendMessage(TextUtils.getConcatenatedCardLinks(toShuffleIn) + " " + TextUtils.be(toShuffleIn) + " shuffled into " + _playerDeck + " deck");

            cardsShuffledCallback();
        }

        return new FullEffectResult(toShuffleIn.size() == _cards.size());
    }

    protected void cardsShuffledCallback() {

    }
}