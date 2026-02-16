package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class DiscardCardToPointAreaAction extends ActionyAction implements DiscardAction, TopLevelSelectableAction {
    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard _discardedCard;


    public DiscardCardToPointAreaAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                        PhysicalCard cardToDiscard) {
        super(cardGame, performingPlayerName, ActionType.DISCARD);
        _performingCard = performingCard;
        _discardedCard = cardToDiscard;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        cardGame.removeCardsFromZone(List.of(_discardedCard));
        if (_discardedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
            stCard.unstop();
        }
        cardGame.getGameState().addCardToZone(cardGame, _discardedCard, Zone.POINT_AREA, _actionContext);
        saveResult(new DiscardCardFromPlayResult(_discardedCard, this), cardGame);
        setAsSuccessful();
    }

    @Override
    public Zone getDestination() {
        return Zone.POINT_AREA;
    }
}