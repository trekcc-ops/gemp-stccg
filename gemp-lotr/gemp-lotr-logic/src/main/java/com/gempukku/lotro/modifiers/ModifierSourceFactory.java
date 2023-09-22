package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModifierSourceFactory {
    private final Map<String, ModifierSourceProducer> modifierProducers = new HashMap<>();

    public ModifierSourceFactory() {
        modifierProducers.put("addactivated", new AddActivated());
        modifierProducers.put("addkeyword", new AddKeyword());
        modifierProducers.put("cancelkeywordbonusfrom", new CancelKeywordBonusFrom());
        modifierProducers.put("cancelstrengthbonusfrom", new CancelStrengthBonusFrom());
        modifierProducers.put("cancelstrengthbonusto", new CancelStrengthBonusTo());
        modifierProducers.put("canplaycardoutofsequence", new CanPlayCardOutOfSequence());
        modifierProducers.put("canplaystackedcards", new CanPlayStackedCards());
        modifierProducers.put("cantbediscarded", new CantBeDiscarded());
        modifierProducers.put("cantbetransferred", new CantBeTransferred());
        modifierProducers.put("cantdiscardcardsfromhandortopofdrawdeck", new CantDiscardCardsFromHandOrTopOfDrawDeck());
        modifierProducers.put("cantlookorrevealhand", new CantLookOrRevealHand());
        modifierProducers.put("cantplaycards", new CantPlayCards());
        modifierProducers.put("cantplayphaseeventsorphasespecialabilities", new CantPlayPhaseEventsOrPhaseSpecialAbilities());
        modifierProducers.put("cantusespecialabilities", new CantUseSpecialAbilities());
        modifierProducers.put("extracosttoplay", new ExtraCostToPlay());
        modifierProducers.put("hastomoveifable", new HasToMoveIfAble());
        modifierProducers.put("itemclassspot", new ItemClassSpot());
        modifierProducers.put("modifycost", new ModifyCost());
        modifierProducers.put("modifymovelimit", new ModifyMoveLimit());
        modifierProducers.put("modifyplayoncost", new ModifyPlayOnCost());
        modifierProducers.put("modifyresistance", new ModifyResistance());
        modifierProducers.put("modifystrength", new ModifyStrength());
        modifierProducers.put("opponentmaynotdiscard", new OpponentMayNotDiscard());
        modifierProducers.put("removekeyword", new RemoveKeyword());
        modifierProducers.put("skipphase", new SkipPhase());


    }

    public ModifierSource getModifier(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(object.get("type"), "type");
        final ModifierSourceProducer modifierSourceProducer = modifierProducers.get(type.toLowerCase());
        if (modifierSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + type);
        return modifierSourceProducer.getModifierSource(object, environment);
    }
}
