package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Objects;

public class EncounterSeedCardAction extends ActionyAction implements TopLevelSelectableAction {
    private final FixedCardResolver _cardTarget;
    private final AttemptMissionAction _parentAction;
    private final AttemptingUnit _attemptingUnit;

    private final int _locationId;

    public EncounterSeedCardAction(DefaultGame cardGame, String encounteringPlayerName, PhysicalCard encounteredCard,
                                   AttemptingUnit attemptingUnit, AttemptMissionAction attemptAction,
                                   int locationId)
            throws InvalidGameLogicException {
        super(cardGame, encounteringPlayerName, ActionType.ENCOUNTER_SEED_CARD);
        try {
            _parentAction = Objects.requireNonNull(attemptAction);
            _cardTarget = new FixedCardResolver(encounteredCard);
            _attemptingUnit = Objects.requireNonNull(attemptingUnit);
            _locationId = locationId;
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
    }

    public EncounterSeedCardAction(DefaultGame cardGame, String encounteringPlayerName, PhysicalCard encounteredCard,
                                   AttemptingUnit attemptingUnit, AttemptMissionAction attemptAction,
                                   int locationId, ActionContext actionContext)
            throws InvalidGameLogicException {
        super(cardGame, encounteringPlayerName, ActionType.ENCOUNTER_SEED_CARD, actionContext);
        try {
            _parentAction = Objects.requireNonNull(attemptAction);
            _cardTarget = new FixedCardResolver(encounteredCard);
            _attemptingUnit = Objects.requireNonNull(attemptingUnit);
            _locationId = locationId;
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        PhysicalCard card = _cardTarget.getCard();
        if (card.getLocationId() == _locationId && !card.isPlacedOnMission()) {
            cardGame.addActionToStack(
                    new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _cardTarget.getCard()));
        }
        setAsSuccessful();
    }

    public AttemptingUnit getAttemptingUnit() { return _attemptingUnit; }
    public PhysicalCard getEncounteredCard() { return _cardTarget.getCard(); }
    public AttemptMissionAction getAttemptAction() { return _parentAction; }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    @Override
    public PhysicalCard getPerformingCard() {
        return _cardTarget.getCard();
    }


}