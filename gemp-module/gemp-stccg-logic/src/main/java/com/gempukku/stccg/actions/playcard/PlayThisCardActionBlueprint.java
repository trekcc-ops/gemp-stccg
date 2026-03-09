package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.actions.targetresolver.PlayFacilityResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collection;
import java.util.List;

public class PlayThisCardActionBlueprint extends DefaultActionBlueprint {

    FilterBlueprint _destinationBlueprint;
    boolean _forFree;
    boolean _onPlanet;

    @JsonCreator
    public PlayThisCardActionBlueprint(@JsonProperty(value = "destination")
                                       FilterBlueprint destinationBlueprint,
                                       @JsonProperty(value = "perGameLimit")
                                       Integer perGameLimit,
                                       @JsonProperty(value = "forFree")
                                       boolean forFree,
                                       @JsonProperty(value = "onPlanet") boolean onPlanet,
                                       @JsonProperty(value = "requirement")
                                       Requirement requirement,
                                       @JsonProperty(value = "immediateEffect")
                                       SubActionBlueprint immediateEffect,
                                       @JsonProperty(value = "discardAfter")
                                       boolean discardAfter
    ) {
        super(new YouPlayerSource());
        _destinationBlueprint = destinationBlueprint;
        if (perGameLimit != null) {
            costs.add(new UsePerGameLimitActionBlueprint(this, perGameLimit));
        }
        _forFree = forFree;
        if (requirement != null) {
            _requirements.add(requirement);
        }
        if (immediateEffect != null) {
            _effects.add(immediateEffect);
        }
        if (discardAfter) {
            _effects.add(new DiscardThisCardSubActionBlueprint());
        }
        _onPlanet = onPlanet;
    }

    public PlayCardAction createAction(DefaultGame cardGame, GameTextContext context) {
        if (!context.acceptsAllRequirements(cardGame, _requirements) ||
                !cardGame.getRules().cardCanEnterPlay(cardGame, context.card(), EnterPlayActionType.PLAY)) {
            return null;
        }
        PlayCardAction action = null;
        if (cardGame instanceof ST1EGame stGame && context.card() instanceof FacilityCard facility) {
            CardFilter additionalFilter = _destinationBlueprint.getFilterable(cardGame, context);
            PlayFacilityResolver resolver = new PlayFacilityResolver(stGame, facility, additionalFilter);
            if (!resolver.cannotBeResolved(cardGame)) {
                action = new PlayFacilityAction(cardGame, facility, resolver);
            }
        } else if (_destinationBlueprint != null) {
            CardFilter destinationFilter = _destinationBlueprint.getFilterable(cardGame, context);
            Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame, destinationFilter);
            if (!destinationOptions.isEmpty()) {
                action = new PlayCardToDestinationAction(cardGame, context.yourName(), context.card(), destinationOptions,
                        context, _onPlanet);
            }
        } else if (cardGame instanceof ST1EGame) {
            action = new PlayCardToDestinationAction(cardGame, context.yourName(), context.card(),
                    new ProxyCoreCard(context.yourName()), context, false);
        }
        if (action != null) {
            if (!_forFree) {
                action.appendCost(new UseNormalCardPlayBlueprint());
            }
            appendSubActions(action);
        }
        return action;
    }

    public Collection<? extends PhysicalCard> getDestinationOptions(GameTextContext context, DefaultGame cardGame) {
        if (_destinationBlueprint != null) {
            CardFilter destinationFilter = _destinationBlueprint.getFilterable(cardGame, context);
            return Filters.filterCardsInPlay(cardGame, destinationFilter);
        } else {
            return List.of(new ProxyCoreCard(context.yourName()));
        }
    }

    public boolean isForFree() {
        return _forFree;
    }
}