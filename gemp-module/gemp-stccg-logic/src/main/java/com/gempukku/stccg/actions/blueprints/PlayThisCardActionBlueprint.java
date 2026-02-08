package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayFacilityAction;
import com.gempukku.stccg.actions.targetresolver.PlayFacilityResolver;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.ArrayList;

public class PlayThisCardActionBlueprint extends DefaultActionBlueprint {

    FilterBlueprint _destinationBlueprint;

    @JsonCreator
    protected PlayThisCardActionBlueprint(@JsonProperty(value = "destination", required = true)
                                          FilterBlueprint destinationBlueprint)
            throws InvalidCardDefinitionException {
        super(0, new ArrayList<>(), new ArrayList<>(), new YouPlayerSource());
        _destinationBlueprint = destinationBlueprint;
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);

        CardFilter additionalFilter = _destinationBlueprint.getFilterable(cardGame, actionContext);
        if (cardGame instanceof ST1EGame stGame && actionContext.card() instanceof FacilityCard facility) {
            PlayFacilityResolver resolver = new PlayFacilityResolver(stGame, facility, additionalFilter);
            if (!resolver.cannotBeResolved(cardGame)) {
                PlayFacilityAction action = new PlayFacilityAction(cardGame, facility, resolver);
                action.appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
                return action;
            }
        }
        return null;
    }

}