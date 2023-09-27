package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TriggerCheckerFactory {
    private final Map<String, TriggerCheckerProducer> triggerCheckers = new HashMap<>();

    public TriggerCheckerFactory() {
        triggerCheckers.put("abouttobekilled", new AboutToBeKilled());
        triggerCheckers.put("abouttodiscard", new AboutToDiscardFromPlay());
        triggerCheckers.put("abouttomoveto", new AboutToMoveTo());
        triggerCheckers.put("requires", new ConditionTrigger());
        triggerCheckers.put("discarded", new Discarded());
        triggerCheckers.put("discardfromdeck", new DiscardFromDeck());
        triggerCheckers.put("discardfromhand", new DiscardFromHand());
        triggerCheckers.put("discardfromhandby", new DiscardFromHandBy());
        triggerCheckers.put("endofphase", new EndOfPhase());
        triggerCheckers.put("endofturn", new EndOfTurn());
        triggerCheckers.put("killed", new Killed());
        triggerCheckers.put("moves", new Moves());
        triggerCheckers.put("movesfrom", new MovesFrom());
        triggerCheckers.put("movesto", new MovesTo());
        triggerCheckers.put("played", new PlayedTriggerCheckerProducer());
        triggerCheckers.put("playedfromstacked", new PlayedFromStacked());
        triggerCheckers.put("playergoesout", new PlayerGoesOut());
        triggerCheckers.put("reconciles", new Reconciles());
        triggerCheckers.put("removedfromplay", new RemovedFromPlay());
        triggerCheckers.put("revealscardfromtopofdrawdeck", new RevealsCardFromTopOfDrawDeck());
        triggerCheckers.put("startofphase", new StartOfPhase());
        triggerCheckers.put("startofturn", new StartOfTurn());
        triggerCheckers.put("transferred", new Transferred());
        triggerCheckers.put("usesspecialability", new UsesSpecialAbility());
    }

    public TriggerChecker<DefaultGame> getTriggerChecker(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String triggerType = FieldUtils.getString(object.get("type"), "type");
        if (triggerType == null)
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final TriggerCheckerProducer<DefaultGame> triggerCheckerProducer = triggerCheckers.get(triggerType.toLowerCase());
        if (triggerCheckerProducer == null)
            throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        return triggerCheckerProducer.getTriggerChecker(object, environment);
    }
}
