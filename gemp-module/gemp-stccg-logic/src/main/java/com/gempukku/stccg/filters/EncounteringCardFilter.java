package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonCreator
    public EncounteringCardFilter(@JsonProperty(value = "encounteredCardId", required = true) int encounteredCardId) {
        _encounteredCardId = encounteredCardId;
    }

    public EncounteringCardFilter(PhysicalCard encounteredCard) {
        this(encounteredCard.getCardId());
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        Stack<Action> actionStack = game.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard().getCardId() == _encounteredCardId &&
            physicalCard instanceof PersonnelCard personnel) {
                return encounterAction.getAttemptingUnit().getAttemptingPersonnel(game).contains(personnel);
            }
        }
        return false;
    }
}