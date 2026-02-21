package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final EncounterSeedCardAction _encounterAction;
    private final MissionRequirement _conditions;
    private boolean _conditionsChecked;
    private boolean _conditionsMet;
    private final boolean _discardDilemma;
    private final PhysicalCard _dilemma;
    private boolean _cardsStopped;
    private final List<SubActionBlueprint> _additionalFailActions = new ArrayList<>();
    private final List<SubActionBlueprint> _additionalSuccessActions = new ArrayList<>();
    private boolean _dilemmaDiscarded;


    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, List<SubActionBlueprint> failDilemmaActions,
                                          List<SubActionBlueprint> successActions, boolean discardDilemma,
                                          GameTextContext context) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA, context);
        _dilemma = dilemma;
        _attemptingUnit = attemptingUnit;
        _additionalFailActions.addAll(failDilemmaActions);
        _conditions = conditions;
        _encounterAction = encounterAction;
        _discardDilemma = discardDilemma;
        _additionalSuccessActions.addAll(successActions);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (!_conditionsChecked) {
            _conditionsChecked = true;
            if (_conditions != null) {
                _conditionsMet = _conditions.canBeMetBy(_attemptingUnit.getAttemptingPersonnel(cardGame), cardGame);
            } else {
                _conditionsMet = true;
            }
        } else {
            if (_conditionsMet) {
                if (!_additionalSuccessActions.isEmpty()) {
                    SubActionBlueprint passAction = _additionalSuccessActions.getFirst();
                    _additionalSuccessActions.remove(passAction);
                    try {
                        List<Action> passActions = passAction.createActions(cardGame, _encounterAction, _actionContext);
                        for (int i = passActions.size() - 1; i >= 0; i--) {
                            cardGame.addActionToStack(passActions.get(i));
                        }
                    } catch(InvalidCardDefinitionException | InvalidGameLogicException | PlayerNotFoundException ignored) {

                    }
                } else {
                    cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma));
                    setAsSuccessful();
                }
            } else {
                if (!_additionalFailActions.isEmpty()) {
                    SubActionBlueprint failAction = _additionalFailActions.getFirst();
                    _additionalFailActions.remove(failAction);
                    try {
                        List<Action> failActions = failAction.createActions(cardGame, _encounterAction, _actionContext);
                        for (int i = failActions.size() - 1; i >= 0; i--) {
                            cardGame.addActionToStack(failActions.get(i));
                        }
                    } catch(InvalidCardDefinitionException | InvalidGameLogicException | PlayerNotFoundException ignored) {

                    }
                } else if (!_cardsStopped) {
                    Collection<ST1EPhysicalCard> cardsToStop =
                            new LinkedList<>(_attemptingUnit.getAttemptingPersonnel(cardGame));
                    if (_attemptingUnit instanceof ShipCard ship) {
                        cardsToStop.add(ship);
                    }
                    _cardsStopped = true;
                    cardGame.addActionToStack(new StopCardsAction(cardGame, _performingPlayerId, cardsToStop));
                } else if (_discardDilemma && !_dilemmaDiscarded) {
                    _dilemmaDiscarded = true;
                    cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma));
                } else {
                    completeActionWithFailure();
                }
            }
        }
    }

    @Override
    public void setAsFailed() {
        _conditionsChecked = true;
        _conditionsMet = false;
        setAsInitiated();
    }

    private void completeActionWithFailure() {
        super.setAsFailed();
        _encounterAction.setAsFailed();
        _encounterAction.getAttemptAction().setAsFailed();
    }
}