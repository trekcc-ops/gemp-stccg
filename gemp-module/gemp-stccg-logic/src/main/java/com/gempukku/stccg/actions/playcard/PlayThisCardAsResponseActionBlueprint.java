package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.DefaultActionBlueprint;
import com.gempukku.stccg.actions.blueprints.DiscardThisCardSubActionBlueprint;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardToDestinationAction;
import com.gempukku.stccg.actions.turn.PlayThisCardAsResponseAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
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
                String yourName = context.card().getOwnerName();
                return new PlayCardToDestinationAction(cardGame, yourName,
                    context.card(), new ProxyCoreCard(yourName),context, false);
            }
        });
        if (discardAfter) {
            _effects.add(new DiscardThisCardSubActionBlueprint());
        }
    }

    @Override
    public PlayThisCardAsResponseAction createAction(DefaultGame cardGame, GameTextContext context) {
        if (context.acceptsAllRequirements(cardGame, _requirements) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, context.card(), EnterPlayActionType.PLAY)) {
            PlayThisCardAsResponseAction action =
                    new PlayThisCardAsResponseAction(cardGame, context.card(), context);
            appendSubActions(action);
            return action;
        }
        return null;
    }

}