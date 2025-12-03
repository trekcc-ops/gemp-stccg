package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class NullifyCardBeingPlayedAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _cardToNullify;

    private final ActionyAction _actionToCancel;
    private boolean _wasCardDiscarded;

    public NullifyCardBeingPlayedAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                        PhysicalCard cardBeingPlayed, ActionyAction playCardAction) {
        super(cardGame, performingPlayerName, "Nullify", ActionType.NULLIFY);
        _performingCard = performingCard;
        _cardToNullify = cardBeingPlayed;
        _actionToCancel = playCardAction;
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_wasCardDiscarded) {
            _actionToCancel.cancel();
            _wasCardDiscarded = true;
            return new DiscardSingleCardAction(cardGame, _performingCard, _performingPlayerId, _cardToNullify);
        }
        setAsSuccessful();
        return getNextAction();
    }

}