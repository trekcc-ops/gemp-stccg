package com.gempukku.stccg.formats;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AbstractGameFormat;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.GameType;

import java.util.List;
import java.util.Map;


@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="code")
public interface GameFormat extends AbstractGameFormat {

    boolean discardPileIsPublic();

    boolean isPlaytest();

    String getName();

    @JsonProperty("code")
    String getCode();
    int getOrder();

    String validateCard(CardBlueprintLibrary library, String cardId);


    List<String> validateDeck(CardBlueprintLibrary library, CardDeck deck);


    CardDeck applyErrata(CardBlueprintLibrary library, CardDeck deck);

    List<String> getBannedCards();

    List<String> getValidCards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    int getHandSize();

    int getMissions();

    boolean hasFixedPlayerOrder();

    boolean isNoShuffle();
    GameType getGameType();

    boolean hallVisible();

    @Override
    String toString();
}