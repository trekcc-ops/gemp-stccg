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

    @Test
    public void testJoinLeagueFirstWeek() {
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
        CollectionType collectionType = new CollectionType("test", "Test Collection");
        for (int i = 20120101; i < 20120108; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            User player = new User(1, "Test", "pass", "u", null, null, null, null);
            data.joinLeague(collectionsManager, player, i);
            //                        @Override
//                        public void describeTo(Description description) {
//                            description.appendText("Expected collection");
//                        }
            Mockito.verify(collectionsManager, new Times(1))
                .addPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType), Mockito.argThat(
                        cards -> {
                            if (Iterables.size(cards.getAll()) != 3)
                                return false;
                            if (cards.getItemCount("(S)FotR - Starter") != 1)
                                return false;
                            if (cards.getItemCount("FotR - Booster") != 6)
                                return false;
                            return cards.getItemCount("1_231") == 2;
                        }
                ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testJoinLeagueSecondWeek() {
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
        CollectionType collectionType = new CollectionType("test", "Test Collection");
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            User player = new User(1, "Test", "pass", "u", null, null, null, null);
            data.joinLeague(collectionsManager, player, i);
            //                        @Override
//                        public void describeTo(Description description) {
//                            description.appendText("Expected collection");
//                        }
            Mockito.verify(collectionsManager, new Times(1)).addPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType), Mockito.argThat(
                    cards -> {
                        if (Iterables.size(cards.getAll()) != 6)
                            return false;
                        if (cards.getItemCount("(S)FotR - Starter") != 1)
                            return false;
                        if (cards.getItemCount("FotR - Booster") != 6)
                            return false;
                        if (cards.getItemCount("1_231") != 2)
                            return false;
                        if (cards.getItemCount("(S)MoM - Starter") != 1)
                            return false;
                        if (cards.getItemCount("MoM - Booster") != 3)
                            return false;
                        return cards.getItemCount("2_51") == 1;
                    }
            ));
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testSwitchToFirstWeek() {
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
        for (int i = 20120101; i < 20120108; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(new HashMap<>());
            int result = data.process(collectionsManager, null, 0, i);
            assertEquals(1, result);
            Mockito.verify(collectionsManager, new Times(1)).getPlayersCollection("test");
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }

    @Test
    public void testProcessMidFirstWeek() {
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
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
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
        CollectionType collectionType = new CollectionType("test", "Test Collection");
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Map<User, CardCollection> playersInLeague = new HashMap<>();
            User player = new User(1, "Test", "pass", "u", null, null, null, null);
            playersInLeague.put(player, new DefaultCardCollection());
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(playersInLeague);
            int result = data.process(collectionsManager, null, 1, i);
            assertEquals(2, result);
            final List<GenericCardItem> expectedToAdd = new ArrayList<>();
            expectedToAdd.add(GenericCardItem.createItem("(S)MoM - Starter", 1));
            expectedToAdd.add(GenericCardItem.createItem("MoM - Booster", 3));
            expectedToAdd.add(GenericCardItem.createItem("2_51", 1));
            Mockito.verify(collectionsManager, new Times(1)).getPlayersCollection("test");
            Mockito.verify(collectionsManager, new Times(1)).addItemsToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(collectionType),
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
        SealedLeagueData data = new SealedLeagueData(_cardLibrary, _formatLibrary, "fotr_block_sealed,20120101,test,Test Collection");
        for (int i = 20120108; i < 20120115; i++) {
            CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
            Mockito.when(collectionsManager.getPlayersCollection("test")).thenReturn(new HashMap<>());
            int result = data.process(collectionsManager, null, 2, i);
            assertEquals(2, result);
            Mockito.verifyNoMoreInteractions(collectionsManager);
        }
    }
}
