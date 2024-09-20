package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrderFeedback;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    List<Integer> getValidSetNums();
    Map<String, String> getValidSets();

    List<String> getBannedCards();

    List<String> getValidCards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    String applyErrata(String bpID);

    List<String> findBaseCards(String bpID);

    int getHandSize();

    JSONDefs.Format Serialize();
    GameProcess getStartingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, DefaultGame game);
}
