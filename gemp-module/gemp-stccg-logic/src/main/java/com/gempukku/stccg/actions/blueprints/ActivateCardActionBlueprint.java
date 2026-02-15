package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ActivateCardActionBlueprint extends DefaultActionBlueprint {

    public ActivateCardActionBlueprint(@JsonProperty(value="limit")
                                        UsageLimitBlueprint usageLimit,
                                       @JsonProperty("requires")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<Requirement> requirements,
                                       @JsonProperty("cost")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                       List<SubActionBlueprint> costs,
                                       @JsonProperty("effect")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
            super(costs, effects, new YouPlayerSource());
            if (requirements != null && !requirements.isEmpty()) {
                _requirements.addAll(requirements);
            }
            if (usageLimit != null) {
                usageLimit.applyLimitToActionBlueprint(this);
            }
    }

    public UseGameTextAction createAction(DefaultGame cardGame, String requestingPlayerName, PhysicalCard card) {
        ActionContext context = new ActionContext(card, requestingPlayerName);
        if (!isActionForPlayer(requestingPlayerName, cardGame, context)) {
            return null;
        } else if (context.acceptsAllRequirements(cardGame, _requirements)) {
            UseGameTextAction action = new UseGameTextAction(cardGame, card, context);
            appendActionToContext(cardGame, action, context);
            return action;
        }
        return null;
    }


}