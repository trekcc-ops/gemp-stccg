package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.PlayFacilityAction;
import com.gempukku.stccg.actions.targetresolver.PlayFacilityResolver;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.Requirement;

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
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
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
                action.appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
            }
        } else if (cardGame instanceof ST1EGame && _destinationBlueprint == null) {
            action = new PlayCardAction(cardGame, thisCard, thisCard, performingPlayerName, Zone.CORE,
                ActionType.PLAY_CARD);
            if (!_forFree)
                action.appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
        }
        if (action != null) {
            appendActionToContext(cardGame, action, actionContext);
        }
        return action;
    }

}