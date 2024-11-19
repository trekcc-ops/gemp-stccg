package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.filterable.GameType;

import java.util.List;
import java.util.Map;

public interface GameFormat {

    boolean discardPileIsPublic();

    boolean isPlaytest();

    String getName();

    String getCode();
    int getOrder();

    String validateCard(String cardId);

    List<String> validateDeck(CardDeck deck);

    CardDeck applyErrata(CardDeck deck);

    List<String> getValidSetIdsAsStrings();
    Map<String, String> getValidSetsAndTheirCards(CardBlueprintLibrary library);

    List<String> getBannedCards();

    List<String> getValidCards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    String applyErrata(String bpID);

    int getHandSize();

    JSONData.Format Serialize();
    int getMissions();

    boolean hasFixedPlayerOrder();

    boolean isNoShuffle();
    GameType getGameType();
}