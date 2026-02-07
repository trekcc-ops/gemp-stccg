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

    public ActivateCardActionBlueprint(@JsonProperty(value="limitPerTurn", defaultValue="0")
                                    int limitPerTurn,
                                       @JsonProperty("requires")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<Requirement> requirements,
                                       @JsonProperty("cost")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                       List<SubActionBlueprint> costs,
                                       @JsonProperty("effect")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
            super(limitPerTurn, costs, effects, new YouPlayerSource());
            if (requirements != null && !requirements.isEmpty()) {
                _requirements.addAll(requirements);
            }
    }

    public UseGameTextAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard card) {
        ActionContext context = new ActionContext(card, card.getOwnerName());
        if (isValid(cardGame, context)) {
            UseGameTextAction action = new UseGameTextAction(cardGame, card, context);
            appendActionToContext(cardGame, action, context);
            return action;
        }
        return null;
    }


}