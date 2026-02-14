package com.gempukku.stccg.requirement.trigger;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.requirement.Requirement;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConditionTriggerChecker.class, name = "condition"),
        @JsonSubTypes.Type(value = EndOfTurnTriggerChecker.class, name = "endOfTurn"),
        @JsonSubTypes.Type(value = KilledTriggerChecker.class, name = "killed"),
        @JsonSubTypes.Type(value = PlayedTriggerChecker.class, name = "played"),
        @JsonSubTypes.Type(value = PlayerGoesOutTriggerChecker.class, name = "playerGoesOut"),
        @JsonSubTypes.Type(value = RandomSelectionInitiationTriggerChecker.class, name = "randomSelectionInitiated"),
        @JsonSubTypes.Type(value = StartOfMissionAttemptTriggerChecker.class, name = "startOfMissionAttempt"),
        @JsonSubTypes.Type(value = StartOfPhaseTriggerChecker.class, name = "startOfPhase"),
        @JsonSubTypes.Type(value = StartOfTurnTriggerChecker.class, name = "startOfTurn"),
        @JsonSubTypes.Type(value = WhenThisCardPlayedTriggerChecker.class, name = "thisCardPlayed")
})
public interface TriggerChecker extends Requirement {
}