package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import org.junit.jupiter.api.Test;

@SuppressWarnings("MagicNumber")
public class LeaguePrizesTest extends AbstractServerTest {

    @Test
    public void test() {
        LeaguePrizes leaguePrizes = new FixedLeaguePrizes(_cardLibrary);
        CardCollection prize = leaguePrizes.getPrizeForLeagueMatchWinner(2);
        for (GenericCardItem stringIntegerEntry : prize.getAll()) {
            System.out.println(stringIntegerEntry.getBlueprintId() + ": " + stringIntegerEntry.getCount());
        }
    }

    @Test
    public void testLeaguePrize() {
        LeaguePrizes leaguePrizes = new FixedLeaguePrizes(_cardLibrary);
        for (int i = 1; i <= 32; i++) {
            System.out.println("Place "+i);
            CardCollection prize = leaguePrizes.getPrizeForLeague(i, CollectionType.ALL_CARDS);
            if (prize != null)
                for (GenericCardItem stringIntegerEntry : prize.getAll()) {
                    System.out.println(stringIntegerEntry.getBlueprintId() + ": " + stringIntegerEntry.getCount());
            }
        }
    }
}