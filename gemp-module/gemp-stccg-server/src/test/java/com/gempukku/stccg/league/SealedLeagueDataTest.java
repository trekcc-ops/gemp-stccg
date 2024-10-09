package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.db.User;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SealedLeagueDataTest extends AbstractServerTest {

    private static final FormatLibrary _formatLibrary = new FormatLibrary(_cardLibrary);
    final SealedLeagueData data = new SealedLeagueData(
            _cardLibrary, _formatLibrary, "testsealed,20120101,test,Test Collection");
    final CollectionType collectionType = new CollectionType("test", "Test Collection");
    final User player = new User(1, "Test", "pass", "u", null, null,
            null, null);

    @Test
    public void testJoinLeagueFirstWeek() {
        for (int i = 20120101; i < 20120108; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            data.joinLeague(collectionsManager, player, i);
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
        }
    }

    @Test
    public void testJoinLeagueSecondWeek() {
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            data.joinLeague(collectionsManager, player, i);
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
        }
    }

    @Test
    public void testSwitchToFirstWeek() {
        for (int i = 20120101; i < 20120108; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(new HashMap<>());
            int result = data.process(collectionsManager, null, 0, i);
            assertEquals(1, result);
            Mockito.verify(
                    collectionsManager, new Times(1)).getPlayersCollection("test");
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testProcessMidFirstWeek() {
        for (int i = 20120101; i < 20120108; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(new HashMap<>());
            int result = data.process(collectionsManager, null, 1, i);
            assertEquals(1, result);
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testSwitchToSecondWeek() {
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Map<User, CardCollection> playersInLeague = new HashMap<>();
            playersInLeague.put(player, new DefaultCardCollection());
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(playersInLeague);
            assertEquals(2, data.process(collectionsManager, null, 1, i));

            final List<GenericCardItem> expectedToAdd = new ArrayList<>();
            expectedToAdd.add(GenericCardItem.createItem("First Contact - Booster", 6));
            expectedToAdd.add(GenericCardItem.createItem("155_079", 1));

            Mockito.verify(collectionsManager, new Times(1))
                    .getPlayersCollection("test");
            Mockito.verify(collectionsManager, new Times(1)).addItemsToPlayerCollection(
                    Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType),
                    Mockito.argThat(
                            (ArgumentMatcher<Collection<GenericCardItem>>) argument -> {
                                if (argument.size() != expectedToAdd.size())
                                    return false;
                                for (GenericCardItem item : expectedToAdd) {
                                    if (!argument.contains(item))
                                        return false;
                                }
                                return true;
                            }));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testProcessMidSecondWeek() {
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(new HashMap<>());
            int result = data.process(collectionsManager, null, 2, i);
            assertEquals(2, result);
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }
}