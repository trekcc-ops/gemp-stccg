package com.gempukku.stccg.league;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.SetDefinition;
import com.gempukku.stccg.collection.MutableCardCollection;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FixedLeaguePrizes implements LeaguePrizes {
    private final Collection<String> _rares = new ArrayList<>();
    private final Collection<String> _allCards = new ArrayList<>();
    private final CardBlueprintLibrary _library;

    public FixedLeaguePrizes(CardBlueprintLibrary library) {
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            _rares.addAll(setDefinition.getCardsOfRarity("R"));
        }
        _allCards.addAll(library.getAllBlueprintIds());
        _library = library;
    }

    @Override
    public final CardCollection getPrizeForLeagueMatchWinner(int winCount) {
        MutableCardCollection winnerPrize = new DefaultCardCollection();
        //noinspection BadOddness
        if (winCount % 2 == 1) {
            winnerPrize.addItem("(S)All Decipher Choice - Booster", 1);
        } else {
            winnerPrize.addItem(_library.getRandomBlueprintId() + "*", 1);
        }
        return winnerPrize;
    }

    @Override
    public final CardCollection getPrizeForLeague(int position, CollectionType collectionType) {
        if (collectionType.equals(CollectionType.ALL_CARDS)) {
            return getPrizeForConstructedLeague(position);
        } else if (collectionType.equals(CollectionType.MY_CARDS) ||
                collectionType.equals(CollectionType.OWNED_TOURNAMENT_CARDS)) {
            return getPrizeForCollectorsLeague(position);
        } else {
            return getPrizeForSealedLeague(position);
        }
    }

    private CardCollection getPrizeForSealedLeague(int position) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getSealedBoosterCount(position));
        addPrizes(prize, getRandomFoil(_rares, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private static int getSealedBoosterCount(int position) {
        if (position < 5)
            return 65 - position * 5;
        else if (position < 9)
            return 40;
        else if (position < 17)
            return 35;
        else if (position < 33)
            return 20;
        else if (position < 65)
            return 10;
        else if (position < 129)
            return 5;
        return 0;
    }

    private CardCollection getPrizeForCollectorsLeague(int position) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getCollectorsBoosterCount(position));
        addPrizes(prize, getRandomFoil(_rares, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private static int getCollectorsBoosterCount(int position) {
        if (position < 5)
            return 35 - position * 5;
        else if (position < 9)
            return 10;
        else if (position < 17)
            return 5;
        else if (position < 33)
            return 2;
        return 0;
    }

    private CardCollection getPrizeForConstructedLeague(int position) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getConstructedBoosterCount(position));
        addPrizes(prize, getRandomFoil(_allCards, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private static int getConstructedBoosterCount(int position) {
        if (position < 5)
            return 12 - position * 2;
        else if (position < 9)
            return 3;
        else if (position < 17)
            return 2;
        else if (position < 33)
            return 1;
        return 0;
    }


    private static int getRandomRareFoilCount(int position) {
        if (position < 4)
            return 4 - position;
        return 0;
    }

    private static void addPrizes(MutableCardCollection leaguePrize, Iterable<String> cards) {
        for (String card : cards)
            leaguePrize.addItem(card, 1);
    }

    private static List<String> getRandomFoil(Iterable<String> list, int count) {
        List<String> result = new LinkedList<>();
        for (String element : list)
            result.add(element + "*");
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result.subList(0, count);
    }

}