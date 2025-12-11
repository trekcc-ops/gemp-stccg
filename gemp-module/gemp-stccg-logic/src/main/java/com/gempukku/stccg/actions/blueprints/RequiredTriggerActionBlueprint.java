package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.List;

public class RequiredTriggerActionBlueprint extends TriggerActionBlueprint {

    @JsonCreator
    private RequiredTriggerActionBlueprint(@JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty(value="triggerDuringSeed", required = true)
                                      boolean triggerDuringSeed,
                                          @JsonProperty("trigger")
                                       TriggerChecker triggerChecker,
                                          @JsonProperty("requires")
                                       List<Requirement> requirements,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("cost")
                                          List<SubActionBlueprint> costs,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("effect")
                                       List<SubActionBlueprint> effects,
                                           @JsonProperty("player")
                                           String playerText) throws InvalidCardDefinitionException {
        super(limitPerTurn, triggerChecker, requirements, costs, effects, triggerDuringSeed,
                (playerText == null) ? new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText));
    }
}