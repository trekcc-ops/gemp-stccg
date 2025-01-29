package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;

public class ThisCardAtMissionCondition implements Condition {
    private final PhysicalCard _thisCard;
    private final CardFilter _missionFilter;

    public ThisCardAtMissionCondition(PhysicalCard thisCard, CardFilter missionFilter) {
        _thisCard = thisCard;
        _missionFilter = missionFilter;
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        try {
            MissionLocation location = _thisCard.getLocation();
            if (location == null)
                return false;
            MissionCard missionCard = location.getMissionForPlayer(_thisCard.getController().getPlayerId());
            return _missionFilter.accepts(cardGame, missionCard);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}