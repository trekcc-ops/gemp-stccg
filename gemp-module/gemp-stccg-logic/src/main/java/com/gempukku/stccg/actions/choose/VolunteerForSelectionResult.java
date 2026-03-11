package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class VolunteerForSelectionResult extends ActionResult {

    @JsonProperty("volunteeringCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _volunteeringCard;

    @JsonProperty("selectingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final PhysicalCard _selectingCard;

    public VolunteerForSelectionResult(DefaultGame cardGame, VolunteerForSelectionAction action,
                                       PhysicalCard volunteeringCard, PhysicalCard selectingCard) {
        super(cardGame, ActionResultType.VOLUNTEERED_FOR_SELECTION, action.getPerformingPlayerId(), action);
        _volunteeringCard = volunteeringCard;
        _selectingCard = selectingCard;
    }

}