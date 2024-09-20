package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModifierSourceFactory {
    private final Map<String, ModifierSourceProducer> modifierProducers = new HashMap<>();

    public ModifierSourceFactory() {
        modifierProducers.put("addkeyword", new AddKeyword());
        modifierProducers.put("cancelkeywordbonusfrom", new CancelKeywordBonusFrom());
        modifierProducers.put("cancelstrengthbonusfrom", new CancelStrengthBonusFrom());
        modifierProducers.put("cancelstrengthbonusto", new CancelStrengthBonusTo());
        modifierProducers.put("canplaycardoutofsequence", new CanPlayCardOutOfSequence());
        modifierProducers.put("canplaystackedcards", new CanPlayStackedCards());
        modifierProducers.put("cantbediscarded", new CantBeDiscarded());
        modifierProducers.put("cantdiscardcardsfromhandortopofdrawdeck", new CantDiscardCardsFromHandOrTopOfDrawDeck());
        modifierProducers.put("cantlookorrevealhand", new CantLookOrRevealHand());
        modifierProducers.put("cantplaycards", new CantPlayCards());
        modifierProducers.put("extracosttoplay", new ExtraCostToPlay());
        modifierProducers.put("gainicon", new AddIcon());
        modifierProducers.put("hastomoveifable", new HasToMoveIfAble());
        modifierProducers.put("modifycost", new ModifyCost());
        modifierProducers.put("modifymovelimit", new ModifyMoveLimit());
        modifierProducers.put("modifyplayoncost", new ModifyPlayOnCost());
        modifierProducers.put("modifystrength", new ModifyStrength());
        modifierProducers.put("opponentmaynotdiscard", new OpponentMayNotDiscard());
        modifierProducers.put("removekeyword", new RemoveKeyword());
        modifierProducers.put("skipphase", new SkipPhase());


    }

    public ModifierSource getModifier(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final String type = environment.getString(object.get("type"), "type");
        final ModifierSourceProducer modifierSourceProducer = modifierProducers.get(type.toLowerCase());
        if (modifierSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + type);
        return modifierSourceProducer.getModifierSource(object, environment);
    }
}
