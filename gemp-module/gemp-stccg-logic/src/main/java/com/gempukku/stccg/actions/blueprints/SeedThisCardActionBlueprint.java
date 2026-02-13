package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedFacilityAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.DestinationBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.SeedQuantityLimitRequirement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SeedThisCardActionBlueprint extends DefaultActionBlueprint {
    private final boolean _toCore;
    private final DestinationBlueprint _destinationBlueprint;

    public SeedThisCardActionBlueprint(@JsonProperty(value = "destinationLocation")
                                       DestinationBlueprint destinationBlueprint,
                                       @JsonProperty(value = "limit")
                                   Integer limit,
                                       @JsonProperty(value = "toCore") boolean toCore
    ) {
        super(new YouPlayerSource());
        _toCore = toCore;
        _destinationBlueprint = destinationBlueprint;
        if (limit != null) {
            costs.add(new UsePerGameLimitActionBlueprint(this, limit));
        }
    }

    @Override
    public SeedCardAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, PlayCardAction.EnterPlayActionType.SEED)) {
            SeedCardAction action;
            if (thisCard instanceof FacilityCard facility && cardGame instanceof ST1EGame stGame) {
                if (_destinationBlueprint != null) {
                    Collection<MissionLocation> destinationOptions =
                            _destinationBlueprint.getDestinationOptions(stGame, performingPlayerName);
                    Map<PhysicalCard, List<Affiliation>> destinationMap =
                            stGame.getRules().getDestinationMapForSeedingFacilityWithGameTextDestinations(
                                    facility, performingPlayerName, destinationOptions, stGame);
                    action = new SeedFacilityAction(cardGame, facility, destinationMap);
                } else {
                    Map<PhysicalCard, List<Affiliation>> destinationMap =
                            stGame.getRules().getDestinationMapForSeedingFacilityPerRules(
                                    facility, performingPlayerName, stGame);
                    action = new SeedFacilityAction(cardGame, facility, destinationMap);
                }
                appendActionToContext(cardGame, action, actionContext);
                return action;
            } else if (_toCore) {
                action = new SeedCardAction(cardGame, thisCard, Zone.CORE, actionContext);
                appendActionToContext(cardGame, action, actionContext);
                return action;
            }
        }
        return null;
    }


    @JsonProperty("limit")
    private void setLimit(int limit) {
        addRequirement(new SeedQuantityLimitRequirement(limit));
    }

}