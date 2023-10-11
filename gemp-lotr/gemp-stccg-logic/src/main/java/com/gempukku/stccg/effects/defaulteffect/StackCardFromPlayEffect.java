package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.utils.DiscardUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StackCardFromPlayEffect extends DefaultEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;
    private final DefaultGame _game;

    public StackCardFromPlayEffect(DefaultGame game, PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
        _game = game;
    }

    @Override
    public String getText() {
        return "Stack " + GameUtils.getFullName(_card) + " on " + GameUtils.getFullName(_stackOn);
    }

    @Override
    public boolean isPlayableInFull() {
        return _card.getZone().isInPlay() && _stackOn.getZone().isInPlay();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();

            Set<PhysicalCard> discardedFromPlayCards = new HashSet<>();
            Set<PhysicalCard> toMoveToDiscardCards = new HashSet<>();

            DiscardUtils.cardsToChangeZones(_game, Collections.singleton(_card), discardedFromPlayCards, toMoveToDiscardCards);

            Set<PhysicalCard> removeFromPlay = new HashSet<>(toMoveToDiscardCards);
            removeFromPlay.add(_card);

            gameState.removeCardsFromZone(_card.getOwner(), removeFromPlay);

            // And put them in new zones (attached and stacked to discard, the card gets stacked on)
            for (PhysicalCard attachedCard : toMoveToDiscardCards)
                gameState.addCardToZone(_game, attachedCard, Zone.DISCARD);

            _game.getGameState().sendMessage(GameUtils.getCardLink(_card) + " is stacked on " + GameUtils.getCardLink(_stackOn));
            _game.getGameState().stackCard(_game, _card, _stackOn);

            // Send the result (attached cards get discarded)
            for (PhysicalCard discardedCard : discardedFromPlayCards)
                _game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
