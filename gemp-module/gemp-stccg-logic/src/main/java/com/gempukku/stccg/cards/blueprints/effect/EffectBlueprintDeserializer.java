package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.PlayCardFromDrawDeck;
import com.gempukku.stccg.cards.blueprints.effect.memorize.ChooseCardAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.memorize.GetCardsFromTopOfDeck;
import com.gempukku.stccg.cards.blueprints.effect.memorize.MemorizeActive;
import com.gempukku.stccg.cards.blueprints.effect.memorize.MemorizeNumber;
import com.gempukku.stccg.cards.blueprints.effect.modifier.AddKeyword;
import com.gempukku.stccg.cards.blueprints.effect.modifier.AddModifier;
import com.gempukku.stccg.cards.blueprints.effect.modifier.ModifyStrength;
import com.gempukku.stccg.cards.blueprints.effect.modifier.RemoveText;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.HashMap;
import java.util.Map;

public class EffectBlueprintDeserializer {
    private final Map<String, EffectAppenderProducer> effectAppenderProducers = new HashMap<>();
    private final CardBlueprintFactory _environment;

    public EffectBlueprintDeserializer(CardBlueprintFactory environment) {
        _environment = environment;
            // Card resolver multi-effect appenders (choose a card and perform an action with it)
        effectAppenderProducers.put("discard", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("discardcardsfromdrawdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("discardfromhand", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("download", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("play", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("playcardfromdiscard", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdeckintohand", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdiscardintohand", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromhandonbottomofplaypile", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("removecardsindiscardfromgame", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("removefromthegame", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("returntohand", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("revealcards", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("revealcardsfromhand", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new CardResolverMultiEffectBlueprintProducer());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new CardResolverMultiEffectBlueprintProducer());
            // Choose card appenders (choose a card and store it for later)
        effectAppenderProducers.put("chooseactivecards", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardAppenderProducer());

            // Misc effect appender producers
        effectAppenderProducers.put("discardcardatrandomfromhand", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("drawcards", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("lookathand", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("placeplayedcardbeneathdrawdeck", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("placetopcardofdrawdeckontopofplaypile", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new MiscEffectAppenderProducer());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new MiscEffectAppenderProducer());

            // Play card effect appender producers
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck()); // multi

        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck()); // unrespondable
        effectAppenderProducers.put("lookatdrawdeck", new LookAtDrawDeck()); // multi, but the first one isn't a card resolver
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.HAND)); // unrespondable
        effectAppenderProducers.put("shuffleplaypileintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.PLAY_PILE)); // unrespondable

            // Modifiers
        effectAppenderProducers.put("addkeyword", new AddKeyword());
        effectAppenderProducers.put("addmodifier", new AddModifier());
        effectAppenderProducers.put("appendcardidstowhileinzone", new AppendCardIdsToWhileInZone());
        effectAppenderProducers.put("costtoeffect", new CostToEffect());
        effectAppenderProducers.put("modifystrength", new ModifyStrength());
        effectAppenderProducers.put("preventable", new PreventableAppenderProducer());
        effectAppenderProducers.put("preventdiscard", new PreventCardEffectAppender());
        effectAppenderProducers.put("removetext", new RemoveText());
            // Other
        effectAppenderProducers.put("activatetribblepower", new ActivateTribblePowerAppender());
        effectAppenderProducers.put("addtrigger", new AddTrigger());
        effectAppenderProducers.put("choice", new Choice());
        effectAppenderProducers.put("foreachplayer", new ForEachPlayer());
        effectAppenderProducers.put("incrementperphaselimit", new IncrementPerPhaseLimit());
        effectAppenderProducers.put("incrementperturnlimit", new IncrementPerTurnLimit());
        effectAppenderProducers.put("memorize", new MemorizeActive());
        effectAppenderProducers.put("memorizenumber", new MemorizeNumber());
        effectAppenderProducers.put("optional", new Optional());
        effectAppenderProducers.put("repeat", new Repeat());
        effectAppenderProducers.put("resetwhileinzonedata", new ResetWhileInZoneData());
        effectAppenderProducers.put("reverseplayerorder", new ReversePlayerOrder());
        effectAppenderProducers.put("scorepoints", new ScorePoints());
        effectAppenderProducers.put("setnexttribble", new SetNextTribble());
        effectAppenderProducers.put("storewhileinzone", new StoreWhileInZone());
    }

    public EffectBlueprint getEffectAppender(JsonNode effectObject) throws InvalidCardDefinitionException {
        final String type = effectObject.get("type").textValue();
        final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
        if (effectAppenderProducer == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        return effectAppenderProducer.createEffectAppender(effectObject, _environment);
    }

}