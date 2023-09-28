package com.gempukku.lotro.game;

import com.gempukku.lotro.adventure.Adventure;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.common.SitesBlock;

import java.util.List;
import java.util.Map;

public interface GameFormat {
    boolean isOrderedSites();

    boolean canCancelRingBearerSkirmish();

    boolean doesNotHaveRuleOfFour();

    boolean hasMulliganRule();

    boolean winWhenShadowReconciles();

    boolean discardPileIsPublic();

    boolean winOnControlling5Sites();

    boolean isPlaytest();

    String getName();
    String getGame();

    String getCode();
    int getOrder();

    String validateCard(String cardId);

    List<String> validateDeck(CardDeck deck);
    String validateDeckForHall(CardDeck deck);

    CardDeck applyErrata(CardDeck deck);

    List<Integer> getValidSets();

    List<String> getBannedCards();

    List<String> getRestrictedCards();

    List<String> getValidCards();

    List<String> getLimit2Cards();

    List<String> getLimit3Cards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    String applyErrata(String bpID);

    List<String> findBaseCards(String bpID);

    SitesBlock getSiteBlock();

    int getHandSize();

    Adventure getAdventure();
    JSONDefs.Format Serialize();
}
