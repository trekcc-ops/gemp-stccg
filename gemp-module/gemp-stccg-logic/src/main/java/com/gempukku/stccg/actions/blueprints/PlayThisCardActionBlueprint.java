package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.PlayCardToDesinationAction;
import com.gempukku.stccg.actions.playcard.PlayFacilityAction;
import com.gempukku.stccg.actions.targetresolver.PlayFacilityResolver;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.common.filterable.Zone;
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

    @JsonCreator
    public PlayThisCardActionBlueprint(@JsonProperty(value = "destination")
                                       FilterBlueprint destinationBlueprint,
                                       @JsonProperty(value = "perGameLimit")
                                       Integer perGameLimit,
                                       @JsonProperty(value = "forFree")
                                       boolean forFree,
                                       @JsonProperty(value = "requirement")
                                       Requirement requirement,
                                       @JsonProperty(value = "immediateEffect")
                                       SubActionBlueprint immediateEffect,
                                       @JsonProperty(value = "discardAfter")
                                       boolean discardAfter
    )
            throws InvalidCardDefinitionException {
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
    }

    @Override
    public PlayCardAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        GameTextContext actionContext = new GameTextContext(thisCard, performingPlayerName);
        if (!isValid(cardGame, actionContext) ||
                !cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, EnterPlayActionType.PLAY)) {
            return null;
        }
        PlayCardAction action = null;
        if (cardGame instanceof ST1EGame stGame && actionContext.card() instanceof FacilityCard facility) {
            CardFilter additionalFilter = _destinationBlueprint.getFilterable(cardGame, actionContext);
            PlayFacilityResolver resolver = new PlayFacilityResolver(stGame, facility, additionalFilter);
            if (!resolver.cannotBeResolved(cardGame)) {
                action = new PlayFacilityAction(cardGame, facility, resolver);
            }
        } else if (_destinationBlueprint != null) {
            CardFilter destinationFilter = _destinationBlueprint.getFilterable(cardGame, actionContext);
            Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame, destinationFilter);
            if (!destinationOptions.isEmpty()) {
                action = new PlayCardToDesinationAction(cardGame, performingPlayerName, thisCard, destinationOptions, actionContext);
            }
        } else if (cardGame instanceof ST1EGame) {
            action = new PlayCardAction(cardGame, thisCard, thisCard, performingPlayerName, Zone.CORE,
                ActionType.PLAY_CARD);
        }
        if (action != null) {
            if (!_forFree)
                action.appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
            appendActionToContext(cardGame, action, actionContext);
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
}