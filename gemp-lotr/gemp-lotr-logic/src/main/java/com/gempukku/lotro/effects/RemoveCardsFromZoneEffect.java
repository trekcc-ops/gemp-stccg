package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;

public class RemoveCardsFromZoneEffect extends AbstractEffect<DefaultGame> {
    private final String _playerPerforming;
    private final PhysicalCard _source;
    private final Collection<PhysicalCard> _cardsToRemove;
    private final Zone _fromZone;

    public RemoveCardsFromZoneEffect(String playerPerforming, PhysicalCard source,
                                     Collection<PhysicalCard> cardsToRemove, Zone fromZone) {
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
        _fromZone = fromZone;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _cardsToRemove.stream().noneMatch(physicalCard -> physicalCard.getZone() != _fromZone);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().removeCardsFromZone(_playerPerforming, _cardsToRemove);
            for (PhysicalCard removedCard : _cardsToRemove)
                game.getGameState().addCardToZone(game, removedCard, Zone.REMOVED);

            game.getGameState().sendMessage(_playerPerforming + " removed " +
                    GameUtils.getAppendedNames(_cardsToRemove) + " from discard using " + GameUtils.getCardLink(_source));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
