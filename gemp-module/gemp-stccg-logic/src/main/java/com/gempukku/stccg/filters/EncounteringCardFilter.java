package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Stack;

public class EncounteringCardFilter implements CardFilter {

    @JsonProperty("encounteredCardId")
    private final int _encounteredCardId;

    public EncounteringCardFilter(PhysicalCard encounteredCard) {
        _encounteredCardId = encounteredCard.getCardId();
    }

    public EncounteringCardFilter(int encounteredCardId) {
        _encounteredCardId = encounteredCardId;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        Stack<Action> actionStack = game.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard().getCardId() == _encounteredCardId &&
            physicalCard instanceof PersonnelCard personnel) {
                return encounterAction.getAttemptingUnit().getAttemptingPersonnel().contains(personnel);
            }
        }
        return false;
    }
}