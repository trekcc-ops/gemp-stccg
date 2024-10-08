package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.HashMap;
import java.util.Map;

public class TriggerCheckerFactory {
    private static final Map<String, TriggerCheckerProducer> triggerCheckers = new HashMap<>();
    @SuppressWarnings("SpellCheckingInspection")
    public TriggerCheckerFactory() {
        triggerCheckers.put("abouttodiscard", new AboutToDiscardFromPlay());
        triggerCheckers.put("requires", new ConditionTrigger());
        triggerCheckers.put("discarded", new Discarded());
        triggerCheckers.put("discardfromdeck", new DiscardFromDeck());
        triggerCheckers.put("discardfromhand", new DiscardFromHand());
        triggerCheckers.put("discardfromhandby", new DiscardFromHandBy());
        triggerCheckers.put("endofphase", new EndOfPhase());
        triggerCheckers.put("movesfrom", new MovesFrom());
        triggerCheckers.put("playergoesout", new PlayerGoesOut());
        triggerCheckers.put("removedfromplay", new RemovedFromPlay());
        triggerCheckers.put("revealscardfromtopofdrawdeck", new RevealsCardFromTopOfDrawDeck());
        triggerCheckers.put("startofphase", new StartOfPhase());
        triggerCheckers.put("startofturn", new StartOfTurn());
    }

    public static TriggerChecker getTriggerChecker(JsonNode object)
            throws InvalidCardDefinitionException {
        if (!object.has("type"))
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final String triggerType = object.get("type").textValue();
        return switch (triggerType) {
            case "endofturn" -> new EndOfTurn().getTriggerChecker(object);
            case "played" -> new PlayedTriggerCheckerProducer().getTriggerChecker(object);
            default -> throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        };
    }


}