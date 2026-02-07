package com.gempukku.stccg.condition.missionrequirements;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.blueprints.MissionRequirementDeserializer;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

@JsonDeserialize(using = MissionRequirementDeserializer.class)
public interface MissionRequirement {

    boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame);

    String toString();
}