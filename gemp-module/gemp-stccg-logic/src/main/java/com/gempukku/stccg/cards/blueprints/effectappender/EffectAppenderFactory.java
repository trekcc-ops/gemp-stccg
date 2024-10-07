package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
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
        effectAppenderProducers.put("download", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("play", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("playcardfromdiscard", new CardResolverMultiEffectAppenderProducer());
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

            // Decisions
        effectAppenderProducers.put("chooseanumber", new ChooseEffectAppenderProducer());
        effectAppenderProducers.put("chooseopponent", new ChooseEffectAppenderProducer());
        effectAppenderProducers.put("chooseplayer", new ChooseEffectAppenderProducer());
        effectAppenderProducers.put("chooseplayerexcept", new ChooseEffectAppenderProducer());
        effectAppenderProducers.put("chooseplayerwithcardsindeck", new ChooseEffectAppenderProducer());
        effectAppenderProducers.put("choosetribblepower", new ChooseEffectAppenderProducer());
            // Choose card appenders (choose a card and store it for later)
        effectAppenderProducers.put("chooseactivecards", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardAppenderProducer());

            // Misc card moving appender producers
        effectAppenderProducers.put("discardbottomcardsfromdeck", new CardStuffAppenderProducer()); // default
        effectAppenderProducers.put("discardcardatrandomfromhand", new CardStuffAppenderProducer());
        effectAppenderProducers.put("discardtopcardfromplaypile", new CardStuffAppenderProducer());
        effectAppenderProducers.put("discardtopcardsfromdeck", new CardStuffAppenderProducer()); // default
        effectAppenderProducers.put("drawcards", new CardStuffAppenderProducer());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new CardStuffAppenderProducer());
        effectAppenderProducers.put("lookathand", new CardStuffAppenderProducer());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new CardStuffAppenderProducer());
        effectAppenderProducers.put("placeplayedcardbeneathdrawdeck", new CardStuffAppenderProducer());
        effectAppenderProducers.put("placetopcardofdrawdeckontopofplaypile", new CardStuffAppenderProducer());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new CardStuffAppenderProducer());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new CardStuffAppenderProducer());
        effectAppenderProducers.put("revealhand", new CardStuffAppenderProducer()); // default

            // Other
        effectAppenderProducers.put("activatetribblepower", new ActivateTribblePowerAppender());
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.HAND)); // unrespondable
        effectAppenderProducers.put("shuffleplaypileintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.PLAY_PILE)); // unrespondable

            // Modifiers
        effectAppenderProducers.put("addkeyword", new AddKeyword());
        effectAppenderProducers.put("addmodifier", new AddModifier());
        effectAppenderProducers.put("costtoeffect", new CostToEffect());
        effectAppenderProducers.put("modifystrength", new ModifyStrength());
        effectAppenderProducers.put("preventable", new PreventableAppenderProducer());
        effectAppenderProducers.put("preventdiscard", new PreventCardEffectAppender());
        effectAppenderProducers.put("removetext", new RemoveText());
    }

    public EffectAppender getEffectAppender(JsonNode effectObject) throws InvalidCardDefinitionException {
        final String type = effectObject.get("type").textValue();
        final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
        if (effectAppenderProducer == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        return effectAppenderProducer.createEffectAppender(effectObject, _environment);
    }

}