package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.actions.blueprints.DownloadCardToDestinationActionBlueprint;
import com.gempukku.stccg.actions.blueprints.UsageLimitBlueprint;
import com.gempukku.stccg.filters.CardTitleFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.requirement.Requirement;

public class SpecialDownloadSkill extends SpecialActionSkill {

    private final ActionBlueprint _downloadActionBlueprint;

    @JsonCreator
    @SuppressWarnings("unused") // Used in JSON deserialization
    private SpecialDownloadSkill(@JsonProperty("downloadCardFilter") FilterBlueprint filterBlueprint,
                                 @JsonProperty("ifCondition") Requirement ifCondition,
                                 @JsonProperty("text") String skillText) {
        _downloadActionBlueprint = new DownloadCardToDestinationActionBlueprint(
                new UsageLimitBlueprint(UsageLimitBlueprint.LimitType.perGame, 1),
                filterBlueprint, true);
        if (ifCondition != null) {
            _downloadActionBlueprint.addRequirement(ifCondition);
        }
    }

    public SpecialDownloadSkill(String text) {
        this(new CardTitleFilterBlueprint(text), null, text);
    }

    public ActionBlueprint getActionBlueprint() {
        return _downloadActionBlueprint;
    }

}