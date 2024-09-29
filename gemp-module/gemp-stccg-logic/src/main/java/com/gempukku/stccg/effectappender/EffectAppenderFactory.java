package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.playcard.PlayCardFromDiscard;
import com.gempukku.stccg.actions.playcard.PlayCardFromDrawDeck;
import com.gempukku.stccg.actions.playcard.PlayCardFromHand;

import java.util.HashMap;
import java.util.Map;

public class EffectAppenderFactory {
    private final Map<String, EffectAppenderProducer> effectAppenderProducers = new HashMap<>();
    private final CardBlueprintFactory _environment;

    public EffectAppenderFactory(CardBlueprintFactory environment) {
        _environment = environment;
            // Applicable to multiple Star Trek games (at least in theory - not all are currently used)
        effectAppenderProducers.put("addtrigger", new AddTrigger());
        effectAppenderProducers.put("choice", new Choice());
        effectAppenderProducers.put("chooseactivecards", new ChooseActiveCards());
        effectAppenderProducers.put("chooseanumber", new ChooseANumber());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardsFromDiscard());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardsFromDrawDeck());
        effectAppenderProducers.put("chooseopponent", new ChooseOpponent());
        effectAppenderProducers.put("chooseplayer", new ChoosePlayer());
        effectAppenderProducers.put("chooseplayerexcept", new ChoosePlayerExcept());
        effectAppenderProducers.put("chooseplayerwithcardsindeck", new ChoosePlayerWithCardsInDeck());
        effectAppenderProducers.put("chooseyesorno", new ChooseYesOrNo());
        effectAppenderProducers.put("discardbottomcardsfromdeck", new DiscardBottomCardFromDeck());
        effectAppenderProducers.put("discardcardatrandomfromhand", new DiscardCardAtRandomFromHand());
        effectAppenderProducers.put("discardcardsfromdrawdeck", new DiscardCardsFromDrawDeck());
        effectAppenderProducers.put("discardfromhand", new DiscardFromHand());
        effectAppenderProducers.put("discardtopcardsfromdeck", new DiscardTopCardFromDeck());
        effectAppenderProducers.put("download", new DownloadCard());
        effectAppenderProducers.put("drawcards", new DrawCards());
        effectAppenderProducers.put("scorepoints", new ScorePoints());
        effectAppenderProducers.put("filtercardsinmemory", new FilterCardsInMemory());
        effectAppenderProducers.put("foreachplayer", new ForEachPlayer());
        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck());
        effectAppenderProducers.put("incrementperphaselimit", new IncrementPerPhaseLimit());
        effectAppenderProducers.put("incrementperturnlimit", new IncrementPerTurnLimit());
        effectAppenderProducers.put("lookatdrawdeck", new LookAtDrawDeck());
        effectAppenderProducers.put("lookathand", new LookAtHand());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new LookAtRandomCardsFromHand());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new LookAtTopCardsOfDrawDeck());
        effectAppenderProducers.put("memorize", new MemorizeActive());
        effectAppenderProducers.put("memorizenumber", new MemorizeNumber());
        effectAppenderProducers.put("optional", new Optional());
        effectAppenderProducers.put("placeplayedcardbeneathdrawdeck", new PlacePlayedCardBeneathDrawDeck());
        effectAppenderProducers.put("play", new PlayCardFromHand());
        effectAppenderProducers.put("playcardfromdiscard", new PlayCardFromDiscard());
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck());
        effectAppenderProducers.put("putcardsfromdeckintohand", new PutCardsFromDeckIntoHand());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new PutCardsFromDeckOnEndOfDrawDeck(EndOfPile.TOP));
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new PutCardsFromDeckOnEndOfDrawDeck(EndOfPile.BOTTOM));
        effectAppenderProducers.put("putcardsfromdiscardintohand", new PutCardsFromDiscardIntoHand());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new PutCardsFromDiscardOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new PutCardsFromDiscardOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new PutCardsFromHandOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new PutCardsFromHandOnTopOfDeck());
        effectAppenderProducers.put("removecardsindiscardfromgame", new RemoveCardsInDiscardFromGame());
        effectAppenderProducers.put("removefromthegame", new RemoveFromTheGame());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new ReorderTopCardsOfDrawDeck());
        effectAppenderProducers.put("repeat", new Repeat());
        effectAppenderProducers.put("returntohand", new ReturnToHand());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new RevealBottomCardsOfDrawDeck());
        effectAppenderProducers.put("revealcards", new RevealCards());
        effectAppenderProducers.put("revealcardsfromhand", new RevealCardsFromHand());
        effectAppenderProducers.put("revealhand", new RevealHand());
        effectAppenderProducers.put("revealrandomcardsfromhand", new RevealRandomCardsFromHand());
        effectAppenderProducers.put("revealtopcardsofdrawdeck", new RevealTopCardsOfDrawDeck());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new ShuffleCardsFromDiscardIntoDrawDeck());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new ShuffleCardsFromHandIntoDrawDeck());
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.HAND));
            // Tribbles-specific
        effectAppenderProducers.put("activatetribblepower", new ActivateTribblePowerAppender());
        effectAppenderProducers.put("choosetribblepower", new ChooseTribblePower());
        effectAppenderProducers.put("discardtopcardfromplaypile", new DiscardTopCardFromPlayPile());
        effectAppenderProducers.put("placetopcardofdrawdeckontopofplaypile", new PlaceTopCardOfDrawDeckOnTopOfPlayPile());
        effectAppenderProducers.put("putcardsfromhandonbottomofplaypile", new PutCardsFromHandOnBottomOfPlayPile());
        effectAppenderProducers.put("reverseplayerorder", new ReversePlayerOrder());
        effectAppenderProducers.put("setnexttribble", new SetNextTribble());
        effectAppenderProducers.put("shuffleplaypileintodrawdeck", new ShuffleCardGroupIntoDrawDeck(Zone.PLAY_PILE));
            // Uncertain - need to review to see if they apply to any Trek games
        effectAppenderProducers.put("addkeyword", new AddKeyword());
        effectAppenderProducers.put("addmodifier", new AddModifier());
        effectAppenderProducers.put("appendcardidstowhileinzone", new AppendCardIdsToWhileInZone());
        effectAppenderProducers.put("conditional", new ConditionalEffect());
        effectAppenderProducers.put("costtoeffect", new CostToEffect());
        effectAppenderProducers.put("discard", new DiscardFromPlay());
        effectAppenderProducers.put("modifystrength", new ModifyStrength());
        effectAppenderProducers.put("preventable", new PreventableAppenderProducer());
        effectAppenderProducers.put("preventdiscard", new PreventCardEffectAppender());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new PutCardsFromPlayOnBottomOfDeck());
        effectAppenderProducers.put("removetext", new RemoveText());
        effectAppenderProducers.put("resetwhileinzonedata", new ResetWhileInZoneData());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new ShuffleCardsFromPlayIntoDrawDeck());
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
