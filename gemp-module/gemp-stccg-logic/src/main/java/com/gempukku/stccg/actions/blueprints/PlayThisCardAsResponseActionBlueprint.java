package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.turn.PlayThisCardAsResponseAction;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.ThisCardIsInHandRequirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.ArrayList;
import java.util.List;

public class PlayThisCardAsResponseActionBlueprint extends DefaultActionBlueprint {

    @JsonCreator
    protected PlayThisCardAsResponseActionBlueprint(@JsonProperty(value = "trigger", required = true)
                                                        TriggerChecker triggerChecker,
                                                    @JsonProperty(value = "immediateEffect", required = true)
                                                    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                                    List<SubActionBlueprint> effects,
                                                    @JsonProperty(value = "discardAfter", required = true)
                                                    boolean discardAfter)
            throws InvalidCardDefinitionException {
        super(new ArrayList<>(), effects, new YouPlayerSource());
        if (triggerChecker != null) {
            _requirements.add(triggerChecker);
            _requirements.add(new ThisCardIsInHandRequirement());
        } else {
            throw new InvalidCardDefinitionException("Unable to add response action with no defined trigger");
        }
        _effects.addFirst(new SubActionBlueprint() {
            @Override
            public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action,
                                              ActionContext actionContext) {
                Action playCardAction = new PlayCardAction(cardGame, actionContext.card(), actionContext.card(),
                    actionContext.card().getOwnerName(), Zone.CORE, ActionType.PLAY_CARD, actionContext);
                return List.of(playCardAction);
            }
        });
        if (discardAfter) {
            _effects.add(new DiscardThisCardSubActionBlueprint());
        }
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, EnterPlayActionType.PLAY)) {
            UseGameTextAction action = new PlayThisCardAsResponseAction(cardGame, thisCard, actionContext);
            appendActionToContext(cardGame, action, actionContext);
            return action;
        }
        return null;
    }

}