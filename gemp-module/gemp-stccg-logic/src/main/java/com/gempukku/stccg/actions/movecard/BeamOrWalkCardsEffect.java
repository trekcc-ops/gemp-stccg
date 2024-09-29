package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;

public class BeamOrWalkCardsEffect extends DefaultEffect {
    private final Collection<PhysicalReportableCard1E> _cardsToBeam;
    private final PhysicalCard _toCard;
    private final PhysicalCard _fromCard;
    private final String _performingPlayerId;
    private final String _actionName;

    public BeamOrWalkCardsEffect(Collection<PhysicalReportableCard1E> cardsToBeam, PhysicalCard fromCard,
                                 PhysicalCard toCard, String playerId, String actionName) {
        super(fromCard.getGame(), playerId);
        _toCard = toCard;
        _cardsToBeam = cardsToBeam;
        _fromCard = fromCard;
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
            if (_fromCard instanceof MissionCard)
                card.leaveAwayTeam();
            if (_toCard instanceof MissionCard mission)
                card.joinEligibleAwayTeam(mission);
        }
        _game.sendMessage(_performingPlayerId + " " + _actionName.toLowerCase() + "ed " +
                TextUtils.plural(_cardsToBeam.size(), "card") + " from " + _fromCard.getCardLink() + " to " +
                _toCard.getCardLink());
        return new FullEffectResult(true);
    }
}