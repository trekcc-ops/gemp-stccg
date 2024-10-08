package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.PlayCardFromDiscard;
import com.gempukku.stccg.cards.blueprints.PlayCardFromDrawDeck;
import com.gempukku.stccg.cards.blueprints.PlayCardFromHand;
import com.gempukku.stccg.cards.blueprints.effect.memorize.*;
import com.gempukku.stccg.cards.blueprints.effect.modifier.AddKeyword;
import com.gempukku.stccg.cards.blueprints.effect.modifier.AddModifier;
import com.gempukku.stccg.cards.blueprints.effect.modifier.ModifyStrength;
import com.gempukku.stccg.cards.blueprints.effect.modifier.RemoveText;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.HashMap;
import java.util.Map;

public class EffectAppenderFactory {
    private final Map<String, EffectAppenderProducer> effectAppenderProducers = new HashMap<>();
    private final CardBlueprintFactory _environment;

    public EffectAppenderFactory(CardBlueprintFactory environment) {
        _environment = environment;
            // Card resolver multi-effect appenders (choose a card and perform an action with it)
        effectAppenderProducers.put("discard", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("discardcardsfromdrawdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("discardfromhand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdeckintohand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdiscardintohand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromhandonbottomofplaypile", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("removecardsindiscardfromgame", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("removefromthegame", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("returntohand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("revealcards", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("revealcardsfromhand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new CardResolverMultiEffectAppenderProducer());
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
        effectAppenderProducers.put("play", new PlayCardFromHand()); // multi
        effectAppenderProducers.put("playcardfromdiscard", new PlayCardFromDiscard()); // multi
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck()); // multi

            // Other card business
        effectAppenderProducers.put("download", new DownloadCard()); // multi

        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck()); // unrespondable
        effectAppenderProducers.put("lookatdrawdeck", new LookAtDrawDeck()); // multi, but the first one isn't a card resolver
        effectAppenderProducers.put("revealrandomcardsfromhand", new RevealRandomCardsFromHand()); // default
        effectAppenderProducers.put("revealtopcardsofdrawdeck", new RevealTopCardsOfDrawDeck()); // default
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

    public EffectAppender getEffectAppender(JsonNode effectObject) throws InvalidCardDefinitionException {
        final String type = effectObject.get("type").textValue();
        final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
        if (effectAppenderProducer == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        return effectAppenderProducer.createEffectAppender(effectObject, _environment);
    }

}