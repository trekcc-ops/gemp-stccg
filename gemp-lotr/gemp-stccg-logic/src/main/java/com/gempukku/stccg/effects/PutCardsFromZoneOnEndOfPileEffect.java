package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Arrays;

public class PutCardsFromZoneOnEndOfPileEffect extends AbstractEffect<DefaultGame> {
    private final PhysicalCard[] physicalCards;
    private final boolean _reveal;
    private final EndOfPile _toEndOfPile;
    private final Zone _fromZone;
    private final Zone _toZone;

    public PutCardsFromZoneOnEndOfPileEffect(boolean reveal, Zone fromZone, Zone toZone, EndOfPile endOfPile,
                                             PhysicalCard... physicalCard) {
        physicalCards = physicalCard;
        _reveal = reveal;
        _fromZone = fromZone;
        _toZone = toZone;
        _toEndOfPile = endOfPile;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Arrays.stream(physicalCards).noneMatch(card -> card.getZone() != _fromZone);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put card from " + _fromZone.getHumanReadable() + " on " + _toEndOfPile + " of " + _toZone.getHumanReadable();
    }

    public String chatMessage(PhysicalCard card) {
        String cardInfoToShow = _reveal ? GameUtils.getCardLink(card) : "a card";
        return card.getOwner() + " puts " + cardInfoToShow + " from " + _fromZone.getHumanReadable() + " on " +
                _toEndOfPile.name().toLowerCase() + " of their " + _toZone.getHumanReadable();
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.removeCardsFromZone(physicalCards[0].getOwner(), Arrays.asList(physicalCards));
            for (PhysicalCard physicalCard : physicalCards) {
                gameState.sendMessage(chatMessage(physicalCard));
                gameState.addCardToZone(game, physicalCard, _toZone, _toEndOfPile);
            }
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}