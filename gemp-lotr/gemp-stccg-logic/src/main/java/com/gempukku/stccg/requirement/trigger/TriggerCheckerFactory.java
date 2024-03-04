package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TriggerCheckerFactory {
    private final Map<String, TriggerCheckerProducer> triggerCheckers = new HashMap<>();

    public TriggerCheckerFactory() {
        //noinspection SpellCheckingInspection
        triggerCheckers.put("abouttodiscard", new AboutToDiscardFromPlay());
        triggerCheckers.put("requires", new ConditionTrigger());
        triggerCheckers.put("discarded", new Discarded());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("discardfromdeck", new DiscardFromDeck());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("discardfromhand", new DiscardFromHand());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("discardfromhandby", new DiscardFromHandBy());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("endofphase", new EndOfPhase());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("endofturn", new EndOfTurn());
        triggerCheckers.put("moves", new Moves());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("movesfrom", new MovesFrom());
        triggerCheckers.put("played", new PlayedTriggerCheckerProducer());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("playedfromstacked", new PlayedFromStacked());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("playergoesout", new PlayerGoesOut());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("removedfromplay", new RemovedFromPlay());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("revealscardfromtopofdrawdeck", new RevealsCardFromTopOfDrawDeck());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("startofphase", new StartOfPhase());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("startofturn", new StartOfTurn());
        triggerCheckers.put("transferred", new Transferred());
        //noinspection SpellCheckingInspection
        triggerCheckers.put("usesspecialability", new UsesSpecialAbility());
    }

    public TriggerChecker getTriggerChecker(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String triggerType = environment.getString(object.get("type"), "type");
        if (triggerType == null)
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final TriggerCheckerProducer triggerCheckerProducer = triggerCheckers.get(triggerType.toLowerCase());
        if (triggerCheckerProducer == null)
            throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        return triggerCheckerProducer.getTriggerChecker(object, environment);
    }
}
