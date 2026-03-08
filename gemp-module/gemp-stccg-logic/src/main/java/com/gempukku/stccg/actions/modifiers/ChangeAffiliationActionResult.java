package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;

public class ChangeAffiliationActionResult extends NoResponseActionResult {

    @JsonProperty("targetCardId")
    private final AffiliatedCard _affiliatedCard;

    @JsonProperty("newAffiliation")
    private final Affiliation _newAffiliation;

    public ChangeAffiliationActionResult(DefaultGame cardGame, String performingPlayerId,
                                         ChangeAffiliationAction action, AffiliatedCard affiliatedCard,
                                         Affiliation newAffiliation) {
        super(cardGame, ActionResultType.CHANGE_AFFILIATION, performingPlayerId, action);
        _affiliatedCard = affiliatedCard;
        _newAffiliation = newAffiliation;
    }
}