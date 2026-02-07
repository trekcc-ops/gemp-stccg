package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.database.User;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ALL")
public class SealedLeagueDataTest extends AbstractServerTest {

    private final static String COLLECTION_TYPE_CODE = "test";
    private final static String SEALED_DEFINITION_FORMAT = "test_sealed";

    LocalDateTime startTimeLocal = LocalDateTime.of(2012, 1, 1, 0, 0);
    ZonedDateTime startTime = ZonedDateTime.of(startTimeLocal, ZoneId.of("UTC"));
    final CollectionType collectionType = new CollectionType(COLLECTION_TYPE_CODE, "Test Collection");
    final User player = new User(1, "Test", "pass", "u", null, null,
            null, null);

    private SealedLeague createLeagueWithNowClock(ZonedDateTime now, int status) {
        Clock nowClock = Clock.fixed(now.toInstant(), now.getZone());
        return new SealedLeague(SEALED_DEFINITION_FORMAT, collectionType, _cardLibrary, startTime,
                _formatLibrary, 0, "Test Sealed League", status, 7, 10, -999,
                3, nowClock);
    }

    private CollectionsManager getMockCollectionsManager(List<User> playersAlreadyInLeague) {
        Map<User, CardCollection> playerCardCollectionMap = new HashMap<>();
        for (User player : playersAlreadyInLeague) {
            playerCardCollectionMap.put(player, new DefaultCardCollection());
        }
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.getPlayersCollection(COLLECTION_TYPE_CODE)).thenReturn(playerCardCollectionMap);
        return collectionsManager;
    }


    @Test
    public void testJoinLeagueFirstWeek() {
        ZonedDateTime nowTime = startTime.plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(1))) {
            CollectionsManager collectionsManager = getMockCollectionsManager(new ArrayList<>());
            League league = createLeagueWithNowClock(nowTime, 0);
            league.joinLeague(collectionsManager, player);
            Mockito.verify(collectionsManager, new Times(1)).addPlayerCollection(
                    Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType),
                    Mockito.argThat(cards -> {
                                if (Iterables.size(cards.getAll()) != 2)
                                    return false;
                                if (cards.getItemCount("Premiere - Booster") != 6)
                                    return false;
                                return cards.getItemCount("155_056") == 1;
                            }
                    ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }

    @Test
    public void testJoinLeagueSecondWeek() {
        ZonedDateTime nowTime = startTime.plusWeeks(1).plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(2))) {
            League league = createLeagueWithNowClock(nowTime, 1); // maybe this should be 0?
            CollectionsManager collectionsManager = getMockCollectionsManager(new ArrayList<>());
            league.joinLeague(collectionsManager, player);
            Mockito.verify(collectionsManager, new Times(1)).addPlayerCollection(
                    Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType),
                    Mockito.argThat(cards -> {
                                if (Iterables.size(cards.getAll()) != 4)
                                    return false;
                                if (cards.getItemCount("Premiere - Booster") != 6)
                                    return false;
                                if (cards.getItemCount("First Contact - Booster") != 6)
                                    return false;
                                if (cards.getItemCount("155_056") != 1)
                                    return false;
                                return cards.getItemCount("155_079") == 1;
                            }
                    ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }

    @Test
    public void testSwitchToFirstWeek() {
        ZonedDateTime nowTime = startTime.plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(1))) {
            SealedLeague league = createLeagueWithNowClock(nowTime, 0);
            CollectionsManager collectionsManager = getMockCollectionsManager(new ArrayList<>());
            league.process(collectionsManager, null);
            assertEquals(1, league.getStatus());
            Mockito.verify(collectionsManager, new Times(1))
                    .getPlayersCollection(COLLECTION_TYPE_CODE);
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }

    @Test
    public void testProcessMidFirstWeek() {
        ZonedDateTime nowTime = startTime.plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(1))) {
            League league = createLeagueWithNowClock(nowTime, 1);
            CollectionsManager collectionsManager = getMockCollectionsManager(new ArrayList<>());
            league.process(collectionsManager, null);
            assertEquals(1, league.getStatus());
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }

    @Test
    public void testSwitchToSecondWeek() {
        ZonedDateTime nowTime = startTime.plusWeeks(1).plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(2))) {
            League league = createLeagueWithNowClock(nowTime, 1);
            CollectionsManager collectionsManager = getMockCollectionsManager(List.of(player));
            league.process(collectionsManager, null);
            assertEquals(2, league.getStatus());

            final List<GenericCardItem> expectedToAdd = List.of(
                    GenericCardItem.createItem("First Contact - Booster", 6),
                    GenericCardItem.createItem("155_079", 1)
            );

            Mockito.verify(collectionsManager, new Times(1))
                    .getPlayersCollection(COLLECTION_TYPE_CODE);
            Mockito.verify(collectionsManager, new Times(1))
                    .addItemsToUserCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player),
                            Mockito.eq(collectionType),
                            Mockito.argThat(cards -> {
                                        if (cards.size() != expectedToAdd.size())
                                            return false;
                                        for (GenericCardItem cardItem : expectedToAdd)
                                            if (!cards.contains(cardItem))
                                                return false;
                                        return true;
                                    }
                            ),
                            Mockito.eq(_cardLibrary));
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }

    @Test
    public void testProcessMidSecondWeek() {
        ZonedDateTime nowTime = startTime.plusWeeks(1).plusNanos(1);
        while (nowTime.isBefore(startTime.plusWeeks(2))) {
            League league = createLeagueWithNowClock(nowTime, 2);
            CollectionsManager collectionsManager = getMockCollectionsManager(new ArrayList<>());
            league.process(collectionsManager, null);
            assertEquals(2, league.getStatus());
            Mockito.verifyNoMoreInteractions(collectionsManager);
            nowTime = nowTime.plusDays(1);
        }
    }
}