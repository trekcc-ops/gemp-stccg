package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DilemmaEncounterGameTextContext extends GameTextContext {

    private final AttemptingUnit _attemptingUnit;
    private final AttemptMissionAction _attemptAction;

    public DilemmaEncounterGameTextContext(PhysicalCard thisCard, String yourName, AttemptingUnit attemptingUnit,
                                           AttemptMissionAction attemptAction) {
        super(thisCard, yourName);
        _attemptingUnit = attemptingUnit;
        _attemptAction = attemptAction;
    }

    public AttemptingUnit attemptingUnit() {
        return _attemptingUnit;
    }

    public AttemptMissionAction attemptAction() {
        return _attemptAction;
    }
}