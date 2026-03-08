package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.NullifyCardAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.targetresolver.FixedCardResolver;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Objects;

public class EncounterSeedCardAction extends ActionWithSubActions implements TopLevelSelectableAction {
    private final FixedCardResolver _cardTarget;
    private final AttemptMissionAction _parentAction;
    private final AttemptingUnit _attemptingUnit;
    private boolean _failedToOvercomeCondition;

    private boolean _nullified;
    private boolean _nullifyAttempted;

    public EncounterSeedCardAction(DefaultGame cardGame, String encounteringPlayerName, PhysicalCard encounteredCard,
                                   AttemptingUnit attemptingUnit, AttemptMissionAction attemptAction,
                                   GameTextContext actionContext)
            throws InvalidGameLogicException {
        super(cardGame, encounteringPlayerName, ActionType.ENCOUNTER_SEED_CARD, actionContext);
        try {
            _parentAction = Objects.requireNonNull(attemptAction);
            _cardTarget = new FixedCardResolver(encounteredCard);
            _attemptingUnit = Objects.requireNonNull(attemptingUnit);
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected final void processEffect(DefaultGame cardGame) {
        if (!_nullifyAttempted) {
            _nullifyAttempted = true;
            if (getEncounteredCard().getNullifyRequirement() != null &&
                    getEncounteredCard().getNullifyRequirement()
                            .canBeMetBy(_attemptingUnit.getAttemptingPersonnel(cardGame), cardGame, _actionContext)) {
                cardGame.getActionsEnvironment().addActionToStack(new NullifyCardAction(cardGame, getEncounteredCard(),
                        _performingPlayerId, new FixedCardResolver(getEncounteredCard())));
                setAsSuccessful();
                _nullified = true;
            }
        } else if (_currentSubAction != null) {
            if (_currentSubAction.wasFailed() && (_currentSubAction instanceof OvercomeDilemmaConditionAction)) {
                _failedToOvercomeCondition = true;
            }
            _processedSubActions.add(_currentSubAction);
            _currentSubAction = null;
        } else if (!_queuedSubActions.isEmpty()) {
            Action nextAction = _queuedSubActions.getFirst().createAction(cardGame, _actionContext);
            if (nextAction != null) {
                _currentSubAction = nextAction;
                cardGame.getActionsEnvironment().addActionToStack(_currentSubAction);
            }
            _queuedSubActions.removeFirst();
        } else {
            if (_failedToOvercomeCondition) {
                setAsFailed();
                _parentAction.setAsFailed();
            } else {
                setAsSuccessful();
            }
        }
        if (wasSuccessful() && !_nullified) {
            PhysicalCard encounteredCard = _cardTarget.getCard();
            if (encounteredCard.getParentCard() == null &&
                    cardGame instanceof ST1EGame stGame &&
                    encounteredCard.getGameLocation(stGame) instanceof MissionLocation missionLocation &&
                    missionLocation.hasCardSeededUnderneath(encounteredCard)
            ) {
                cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, encounteredCard));
            }
        }
    }

    public AttemptingUnit getAttemptingUnit() { return _attemptingUnit; }
    public PhysicalCard getEncounteredCard() { return _cardTarget.getCard(); }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    @Override
    public PhysicalCard getPerformingCard() {
        return _cardTarget.getCard();
    }


}