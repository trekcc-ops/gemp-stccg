package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.JSONData;

import java.util.List;
import java.util.Map;

public interface GameFormat {

    boolean discardPileIsPublic();

    boolean isPlaytest();

    String getName();
    String getGameType();

    String getCode();
    int getOrder();

    String validateCard(String cardId);

    List<String> validateDeck(CardDeck deck);
    String validateDeckForHall(CardDeck deck);

    CardDeck applyErrata(CardDeck deck);

    List<Integer> getValidSetIds();
    Map<String, String> getValidSets();

    List<String> getBannedCards();

    List<String> getValidCards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    String applyErrata(String bpID);

    int getHandSize();

    JSONData.Format Serialize();
    String serializeForHall() throws CardNotFoundException;
}