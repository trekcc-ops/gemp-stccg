package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;

public class ThisCardAtMissionCondition implements Condition {

    @JsonProperty("thisCardId")
    private final int _thisCardId;

    @JsonProperty("missionFilter")
    private final CardFilter _missionFilter;

    public ThisCardAtMissionCondition(PhysicalCard thisCard, CardFilter missionFilter) {
        _thisCardId = thisCard.getCardId();
        _missionFilter = missionFilter;
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        try {
            PhysicalCard thisCard = cardGame.getCardFromCardId(_thisCardId);
            if (thisCard.getGameLocation() instanceof MissionLocation missionLocation) {
                MissionCard missionCard = missionLocation.getMissionForPlayer(thisCard.getControllerName());
                return _missionFilter.accepts(cardGame, missionCard);
            } else {
                return false;
            }
        } catch(InvalidGameLogicException | CardNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}