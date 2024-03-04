package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalMissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.TextUtils;

import java.util.Collection;

public class BeamOrWalkCardsEffect extends DefaultEffect {
    private final Collection<PhysicalReportableCard1E> _cardsToBeam;
    private final PhysicalCard _toCard;
    private final PhysicalCard _fromCard;
    private final DefaultGame _game;
    private final String _performingPlayerId;
    private final String _actionName;

    public BeamOrWalkCardsEffect(Collection<PhysicalReportableCard1E> cardsToBeam, PhysicalCard fromCard,
                                 PhysicalCard toCard, String playerId, String actionName) {
        super(playerId);
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
        for (PhysicalReportableCard1E card : _cardsToBeam) {
            card.attachToCardAtLocation(_toCard);
            if (_fromCard instanceof PhysicalMissionCard)
                card.leaveAwayTeam();
            if (_toCard instanceof PhysicalMissionCard mission)
                card.joinEligibleAwayTeam(mission);
        }
        _game.getGameState().sendMessage(_performingPlayerId + " " + _actionName.toLowerCase() + "ed " +
                TextUtils.plural(_cardsToBeam.size(), "card") + " from " + _fromCard.getCardLink() + " to " +
                _toCard.getCardLink());
        return new FullEffectResult(true);
    }
}