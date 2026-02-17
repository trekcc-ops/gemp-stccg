package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.actions.blueprints.DownloadCardToDestinationActionBlueprint;
import com.gempukku.stccg.actions.blueprints.UsageLimitBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardTitleFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.requirement.Requirement;

public class SpecialDownloadSkill extends Skill {

    private final ActionBlueprint _downloadActionBlueprint;

    @JsonCreator
    private SpecialDownloadSkill(@JsonProperty("downloadCardFilter") FilterBlueprint filterBlueprint,
                                 @JsonProperty("ifCondition") Requirement ifCondition,
                                 @JsonProperty("text") String skillText) throws InvalidCardDefinitionException {
        _downloadActionBlueprint = new DownloadCardToDestinationActionBlueprint(new UsageLimitBlueprint(UsageLimitBlueprint.LimitType.perGame, 1),
                filterBlueprint, true);
        if (ifCondition != null) {
            _downloadActionBlueprint.addRequirement(ifCondition);
        }
    }

    public SpecialDownloadSkill(String text) throws InvalidCardDefinitionException {
        _downloadActionBlueprint = new DownloadCardToDestinationActionBlueprint(new UsageLimitBlueprint(UsageLimitBlueprint.LimitType.perGame, 1),
                new CardTitleFilterBlueprint(text), true);
    }

    public ActionBlueprint getActionBlueprint(PhysicalCard thisCard) {
        return _downloadActionBlueprint;
    }

}