package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.turn.PlayThisCardAsResponseAction;
import com.gempukku.stccg.cards.GameTextContext;
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
            public Action createAction(DefaultGame cardGame, GameTextContext context) {
                return new PlayCardAction(cardGame, context.card(), context.card(),
                    context.card().getOwnerName(), Zone.CORE, ActionType.PLAY_CARD, context);
            }
        });
        if (discardAfter) {
            _effects.add(new DiscardThisCardSubActionBlueprint());
        }
    }

    @Override
    public PlayThisCardAsResponseAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        GameTextContext actionContext = new GameTextContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, EnterPlayActionType.PLAY)) {
            PlayThisCardAsResponseAction action = new PlayThisCardAsResponseAction(cardGame, thisCard, actionContext);
            appendSubActions(action);
            return action;
        }
        return null;
    }

}