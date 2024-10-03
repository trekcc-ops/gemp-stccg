package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.PlayCardFromDiscard;
import com.gempukku.stccg.cards.blueprints.PlayCardFromDrawDeck;
import com.gempukku.stccg.cards.blueprints.PlayCardFromHand;
import com.gempukku.stccg.cards.blueprints.effectappender.memorize.*;
import com.gempukku.stccg.cards.blueprints.effectappender.modifier.AddKeyword;
import com.gempukku.stccg.cards.blueprints.effectappender.modifier.AddModifier;
import com.gempukku.stccg.cards.blueprints.effectappender.modifier.ModifyStrength;
import com.gempukku.stccg.cards.blueprints.effectappender.modifier.RemoveText;
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
        effectAppenderProducers.put("putcardsfromdeckintohand", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("removecardsindiscardfromgame", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("removefromthegame", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new CardResolverMultiEffectAppenderProducer());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new CardResolverMultiEffectAppenderProducer());
            // Choose card appenders (choose a card and store it for later)
        effectAppenderProducers.put("chooseactivecards", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardAppenderProducer());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardAppenderProducer());

            // Card resolver misc.
        effectAppenderProducers.put("discardbottomcardsfromdeck", new DiscardBottomCardFromDeck());
        effectAppenderProducers.put("discardcardatrandomfromhand", new DiscardCardAtRandomFromHand());
        effectAppenderProducers.put("discardfromhand", new DiscardFromHand());
        effectAppenderProducers.put("discardtopcardfromplaypile", new DiscardTopCardFromPlayPile());
        effectAppenderProducers.put("discardtopcardsfromdeck", new DiscardTopCardFromDeck());
        effectAppenderProducers.put("download", new DownloadCard());
        effectAppenderProducers.put("drawcards", new DrawCards());
        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck());
        effectAppenderProducers.put("lookatdrawdeck", new LookAtDrawDeck());
        effectAppenderProducers.put("lookathand", new LookAtHand());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new LookAtRandomCardsFromHand());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new LookAtTopCardsOfDrawDeck());
        effectAppenderProducers.put("placeplayedcardbeneathdrawdeck", new PlacePlayedCardBeneathDrawDeck());
        effectAppenderProducers.put("placetopcardofdrawdeckontopofplaypile", new PlaceTopCardOfDrawDeckOnTopOfPlayPile());
        effectAppenderProducers.put("play", new PlayCardFromHand());
        effectAppenderProducers.put("playcardfromdiscard", new PlayCardFromDiscard());
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck());
        effectAppenderProducers.put("putcardsfromdiscardintohand", new PutCardsFromDiscardIntoHand());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new PutCardsFromDiscardOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new PutCardsFromHandOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromhandonbottomofplaypile", new PutCardsFromHandOnBottomOfPlayPile());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new PutCardsFromPlayOnBottomOfDeck());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new ReorderTopCardsOfDrawDeck());
        effectAppenderProducers.put("returntohand", new ReturnToHand());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new RevealBottomCardsOfDrawDeck());
        effectAppenderProducers.put("revealcards", new RevealCards());
        effectAppenderProducers.put("revealcardsfromhand", new RevealCardsFromHand());
        effectAppenderProducers.put("revealhand", new RevealHand());
        effectAppenderProducers.put("revealrandomcardsfromhand", new RevealRandomCardsFromHand());
        effectAppenderProducers.put("revealtopcardsofdrawdeck", new RevealTopCardsOfDrawDeck());
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.HAND));
        effectAppenderProducers.put("shuffleplaypileintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.PLAY_PILE));

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
        effectAppenderProducers.put("chooseanumber", new ChooseANumber());
        effectAppenderProducers.put("chooseopponent", new ChooseOpponent());
        effectAppenderProducers.put("chooseplayer", new ChoosePlayer());
        effectAppenderProducers.put("chooseplayerexcept", new ChoosePlayerExcept());
        effectAppenderProducers.put("chooseplayerwithcardsindeck", new ChoosePlayerWithCardsInDeck());
        effectAppenderProducers.put("choosetribblepower", new ChooseTribblePower());
        effectAppenderProducers.put("chooseyesorno", new ChooseYesOrNo());
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

    public EffectAppender[] getEffectAppenders(JsonNode[] effectArray) throws InvalidCardDefinitionException {
        EffectAppender[] result = new EffectAppender[effectArray.length];
        for (int i = 0; i < result.length; i++) {
            final String type = effectArray[i].get("type").textValue();
            final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
            if (effectAppenderProducer == null)
                throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
            result[i] = effectAppenderProducer.createEffectAppender(effectArray[i], _environment);
        }
        return result;
    }

}
