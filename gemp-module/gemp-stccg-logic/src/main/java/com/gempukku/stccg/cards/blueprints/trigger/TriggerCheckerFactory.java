package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class TriggerCheckerFactory {

    public static TriggerChecker getTriggerChecker(JsonNode object)
            throws InvalidCardDefinitionException {
        if (!object.has("type"))
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final String triggerType = object.get("type").textValue();
        final TriggerCheckerProducer triggerCheckerProducer = switch(triggerType.toLowerCase()) {
            case "abouttodiscard" -> new AboutToDiscardFromPlay();
            case "discarded" -> new Discarded();
            case "discardfromdeck" -> new DiscardFromDeck();
            case "discardfromhand" -> new DiscardFromHand();
            case "discardfromhandby" -> new DiscardFromHandBy();
            case "endofphase" -> new EndOfPhase();
            case "endofturn" -> new EndOfTurn();
            case "movesfrom" -> new MovesFrom();
            case "played" -> new PlayedTriggerCheckerProducer();
            case "playergoesout" -> new PlayerGoesOut();
            case "removedfromplay" -> new RemovedFromPlay();
            case "requires" -> new ConditionTrigger();
            case "revealscardfromtopofdrawdeck" -> new RevealsCardFromTopOfDrawDeck();
            case "startofphase" -> new StartOfPhase();
            case "startofturn" -> new StartOfTurn();
            default -> throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        };
        return triggerCheckerProducer.getTriggerChecker(object);
    }
}