package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.HashMap;
import java.util.Map;

public class TriggerCheckerFactory {
    private final Map<String, TriggerCheckerProducer> triggerCheckers = new HashMap<>();
    @SuppressWarnings("SpellCheckingInspection")
    public TriggerCheckerFactory() {
        triggerCheckers.put("abouttodiscard", new AboutToDiscardFromPlay());
        triggerCheckers.put("requires", new ConditionTrigger());
        triggerCheckers.put("discarded", new Discarded());
        triggerCheckers.put("discardfromdeck", new DiscardFromDeck());
        triggerCheckers.put("discardfromhand", new DiscardFromHand());
        triggerCheckers.put("discardfromhandby", new DiscardFromHandBy());
        triggerCheckers.put("endofphase", new EndOfPhase());
        triggerCheckers.put("endofturn", new EndOfTurn());
        triggerCheckers.put("movesfrom", new MovesFrom());
        triggerCheckers.put("played", new PlayedTriggerCheckerProducer());
        triggerCheckers.put("playergoesout", new PlayerGoesOut());
        triggerCheckers.put("removedfromplay", new RemovedFromPlay());
        triggerCheckers.put("revealscardfromtopofdrawdeck", new RevealsCardFromTopOfDrawDeck());
        triggerCheckers.put("startofphase", new StartOfPhase());
        triggerCheckers.put("startofturn", new StartOfTurn());
    }

    public TriggerChecker getTriggerChecker(JsonNode object)
            throws InvalidCardDefinitionException {
        if (!object.has("type"))
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final String triggerType = object.get("type").textValue();
        final TriggerCheckerProducer triggerCheckerProducer = triggerCheckers.get(triggerType.toLowerCase());
        if (triggerCheckerProducer == null)
            throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        return triggerCheckerProducer.getTriggerChecker(object);
    }


}