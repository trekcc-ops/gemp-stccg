package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class NullifyCardBeingPlayedAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _cardToNullify;

    private final ActionyAction _actionToCancel;
    private boolean _wasCardDiscarded;

    public NullifyCardBeingPlayedAction(PhysicalCard performingCard, Player performingPlayer,
                                        PhysicalCard cardBeingPlayed, ActionyAction playCardAction) {
        super(performingCard.getGame(), performingPlayer, ActionType.NULLIFY);
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_wasCardDiscarded) {
            _actionToCancel.cancel();
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            _wasCardDiscarded = true;
            return new DiscardSingleCardAction(_performingCard, performingPlayer, _cardToNullify);
        }
        setAsSuccessful();
        return getNextAction();
    }

}