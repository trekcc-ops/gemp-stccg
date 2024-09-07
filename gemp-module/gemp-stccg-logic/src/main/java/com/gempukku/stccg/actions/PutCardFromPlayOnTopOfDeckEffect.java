package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.discard.DiscardUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PutCardFromPlayOnTopOfDeckEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final DefaultGame _game;

    public PutCardFromPlayOnTopOfDeckEffect(DefaultGame game, PhysicalCard physicalCard) {
        super(physicalCard.getOwnerName());
        _physicalCard = physicalCard;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _physicalCard.getZone().isInPlay();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            Set<PhysicalCard> discardedCards = new HashSet<>();
            Set<PhysicalCard> toGoToDiscardCards = new HashSet<>();

            DiscardUtils.cardsToChangeZones(_game, Collections.singleton(_physicalCard), discardedCards, toGoToDiscardCards);

            GameState gameState = _game.getGameState();

            Set<PhysicalCard> removeFromPlay = new HashSet<>(toGoToDiscardCards);
            removeFromPlay.add(_physicalCard);

            gameState.removeCardsFromZone(_physicalCard.getOwnerName(), removeFromPlay);

            gameState.putCardOnTopOfDeck(_physicalCard);
            for (PhysicalCard discardedCard : discardedCards) {
                _game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
            }
            for (PhysicalCard toGoToDiscardCard : toGoToDiscardCards)
                gameState.addCardToZone(toGoToDiscardCard, Zone.DISCARD);

            gameState.sendMessage(_physicalCard.getOwnerName() + " puts " + _physicalCard.getCardLink() + " from play on the top of deck");

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    @Override
    public String getText() {
        return "Put " + _physicalCard.getFullName() + " from play on top of deck";
    }

}
