package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardToDestinationAction;
import com.gempukku.stccg.actions.playcard.SeedFacilityAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SeedThisCardActionBlueprint extends DefaultActionBlueprint {
    private final boolean _toCore;
    private final boolean _onPlanet;
    private final DestinationBlueprint _destinationBlueprint;
    private final FilterBlueprint _destinationFilter;

    public SeedThisCardActionBlueprint(@JsonProperty(value = "destination")
                                       DestinationBlueprint destinationBlueprint,
                                       @JsonProperty(value = "destinationFilter")
                                       FilterBlueprint destinationFilter,
                                       @JsonProperty(value = "limit")
                                   Integer limit,
                                       @JsonProperty(value = "onPlanet") boolean onPlanet,
                                       @JsonProperty(value = "toCore") boolean toCore
    ) {
        super(new YouPlayerSource());
        _onPlanet = onPlanet;
        _toCore = toCore;
        _destinationBlueprint = destinationBlueprint;
        _destinationFilter = destinationFilter;
        if (limit != null) {
            costs.add(new UsePerGameLimitActionBlueprint(this, limit));
        }
    }

    @Override
    public SeedCardAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard thisCard) {
        GameTextContext actionContext = new GameTextContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, EnterPlayActionType.SEED)) {
            SeedCardAction action;
            if (thisCard instanceof FacilityCard facility && cardGame instanceof ST1EGame stGame) {
                if (_destinationBlueprint instanceof AtLocationDestinationBlueprint locationBlueprint) {
                    Collection<MissionLocation> destinationOptions =
                            locationBlueprint.getDestinationOptions(stGame, performingPlayerName);
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
            } else if (_destinationFilter != null) {
                CardFilter destination = _destinationFilter.getFilterable(cardGame, actionContext);
                Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame, destination);
                if (!destinationOptions.isEmpty()) {
                    action = new SeedCardToDestinationAction(cardGame, performingPlayerName,
                        List.of(thisCard), destinationOptions, thisCard, _onPlanet);
                    appendActionToContext(cardGame, action, actionContext);
                    return action;
                }
            }
        }
        return null;
    }

}