package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

public class ClassificationFilter implements CardFilter {

    @JsonProperty("classification")
    private final SkillName _classification;

    @JsonCreator
    public ClassificationFilter(@JsonProperty("classification") SkillName classification) {
        _classification = classification;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof PersonnelCard personnelCard && personnelCard.getClassification() == _classification;
    }
}