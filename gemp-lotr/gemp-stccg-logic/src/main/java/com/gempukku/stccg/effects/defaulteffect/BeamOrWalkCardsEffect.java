package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public class BeamOrWalkCardsEffect extends DefaultEffect {
    private final Collection<PhysicalCard> _cardsToBeam;
    private final PhysicalCard _toCard;
    private final PhysicalCard _fromCard;
    private final DefaultGame _game;
    private final String _performingPlayerId;
    private final String _actionName;

    public BeamOrWalkCardsEffect(Collection<PhysicalCard> cardsToBeam, PhysicalCard fromCard,
                                 PhysicalCard toCard, String playerId, String actionName) {
        _toCard = toCard;
        _cardsToBeam = cardsToBeam;
        _fromCard = fromCard;
        _game = fromCard.getGame();
        _performingPlayerId = playerId;
        _actionName = actionName;
    }

    @Override
    public String getText() {
        return _actionName + " cards";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        for (PhysicalCard card : _cardsToBeam) {
            card.attachToCardAtLocation(_toCard);
        }
        if (_toCard instanceof PhysicalMissionCard)
            ((PhysicalMissionCard) _toCard).organizeAwayTeamsOnSurface();
        if (_fromCard instanceof PhysicalMissionCard)
            ((PhysicalMissionCard) _fromCard).organizeAwayTeamsOnSurface();
        _game.getGameState().sendMessage(_performingPlayerId + " " + _actionName.toLowerCase() + "ed " +
                GameUtils.plural(_cardsToBeam.size(), "card") + " from " + _fromCard.getCardLink() + " to " +
                _toCard.getCardLink());
        return new FullEffectResult(true);
    }
}