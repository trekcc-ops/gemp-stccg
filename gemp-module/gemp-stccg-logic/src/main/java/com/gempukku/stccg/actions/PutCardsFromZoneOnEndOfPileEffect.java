package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Arrays;

public class PutCardsFromZoneOnEndOfPileEffect extends DefaultEffect {
    private final PhysicalCard[] physicalCards;
    private final boolean _reveal;
    private final EndOfPile _toEndOfPile;
    private final Zone _fromZone;
    private final Zone _toZone;
    private final DefaultGame _game;

    public PutCardsFromZoneOnEndOfPileEffect(DefaultGame game, boolean reveal, Zone fromZone, Zone toZone,
                                             EndOfPile endOfPile, PhysicalCard... physicalCard) {
        super(physicalCard[0]);
        physicalCards = physicalCard;
        _reveal = reveal;
        _fromZone = fromZone;
        _toZone = toZone;
        _toEndOfPile = endOfPile;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return Arrays.stream(physicalCards).noneMatch(card -> card.getZone() != _fromZone);
    }

    @Override
    public String getText() {
        return "Put card from " + _fromZone.getHumanReadable() + " on " + _toEndOfPile + " of " + _toZone.getHumanReadable();
    }

    public String chatMessage(PhysicalCard card) {
        String cardInfoToShow = _reveal ? card.getCardLink() : "a card";
        return card.getOwnerName() + " puts " + cardInfoToShow + " from " + _fromZone.getHumanReadable() + " on " +
                _toEndOfPile.name().toLowerCase() + " of their " + _toZone.getHumanReadable();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            gameState.removeCardsFromZone(physicalCards[0].getOwnerName(), Arrays.asList(physicalCards));
            for (PhysicalCard physicalCard : physicalCards) {
                gameState.sendMessage(chatMessage(physicalCard));
                gameState.addCardToZone(physicalCard, _toZone, _toEndOfPile);
            }
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}