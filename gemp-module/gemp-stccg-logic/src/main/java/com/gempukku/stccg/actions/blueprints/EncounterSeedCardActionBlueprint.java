package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EncounterSeedCardActionBlueprint extends DefaultActionBlueprint {

    @JsonCreator
    private EncounterSeedCardActionBlueprint(@JsonProperty("effect")
                                            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                            List<SubActionBlueprint> effects) {
        super(new ArrayList<>(), Objects.requireNonNullElse(effects, new LinkedList<>()),
                new YouPlayerSource());
    }

    public EncounterSeedCardAction createAction(DefaultGame cardGame, GameTextContext context) {
        try {
            if (context instanceof DilemmaEncounterGameTextContext dilemmaContext) {
                EncounterSeedCardAction encounterAction =
                        new EncounterSeedCardAction(cardGame, context.yourName(), context.card(),
                                dilemmaContext.attemptingUnit(), dilemmaContext.attemptAction(), context);
                _effects.forEach(encounterAction::appendSubAction);
                return encounterAction;
            } else {
                throw new InvalidGameLogicException("Cannot create encounter action with provided context object");
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return null;
        }
    }
}