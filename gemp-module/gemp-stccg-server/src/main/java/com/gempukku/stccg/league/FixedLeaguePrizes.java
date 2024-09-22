package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FixedLeaguePrizes implements LeaguePrizes {
    private final List<String> _rares = new ArrayList<>();
    private final List<String> _allCards = new ArrayList<>();
    private final CardBlueprintLibrary _library;

    public FixedLeaguePrizes(CardBlueprintLibrary library) {
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            _rares.addAll(setDefinition.getCardsOfRarity("R"));
        }
        _allCards.addAll(library.getAllBlueprintIds());
        _library = library; // TODO - Not sure it's helpful to save this as a class member once rarities work
    }

    @Override
    public CardCollection getPrizeForLeagueMatchWinner(int winCount, int gamesPlayed) {
        DefaultCardCollection winnerPrize = new DefaultCardCollection();
        if (winCount % 2 == 1) {
            winnerPrize.addItem("(S)All Decipher Choice - Booster", 1);
        } else {
            winnerPrize.addItem(_library.getRandomBlueprintId() + "*", 1);
        }
        return winnerPrize;
    }

    @Override
    public CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie) {
        return null;
    }

    @Override
    public CardCollection getPrizeForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType) {
        if (collectionType.equals(CollectionType.ALL_CARDS)) {
            return getPrizeForConstructedLeague(position, playersCount, gamesPlayed, maxGamesPlayed);
        } else if (collectionType.equals(CollectionType.MY_CARDS) || collectionType.equals(CollectionType.OWNED_TOURNAMENT_CARDS)) {
            return getPrizeForCollectorsLeague(position, playersCount, gamesPlayed, maxGamesPlayed);
        } else {
            return getPrizeForSealedLeague(position);
        }
    }

    @Override
    public CardCollection getTrophiesForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)Tengwar", getTengwarCount(position));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private CardCollection getPrizeForSealedLeague(int position) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getSealedBoosterCount(position));
        addPrizes(prize, getRandomFoil(_rares, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private int getSealedBoosterCount(int position) {
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

    private CardCollection getPrizeForCollectorsLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getCollectorsBoosterCount(position));
        addPrizes(prize, getRandomFoil(_rares, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private int getCollectorsBoosterCount(int position) {
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

    //1st - 10 boosters, 4 tengwar, 3 foil rares
//2nd - 8 boosters, 3 tengwar, 2 foil rares
//3rd - 6 boosters, 2 tengwar, 1 foil rare
//4th - 4 boosters, 1 tengwar
//5th-8th - 3 boosters
//9th-16th - 2 boosters
//17th-32nd - 1 boosters
    private CardCollection getPrizeForConstructedLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addItem("(S)All Decipher Choice - Booster", getConstructedBoosterCount(position));
        addPrizes(prize, getRandomFoil(_allCards, getRandomRareFoilCount(position)));
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    private int getConstructedBoosterCount(int position) {
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


    private int getRandomRareFoilCount(int position) {
        if (position < 4)
            return 4 - position;
        return 0;
    }

    private void addPrizes(DefaultCardCollection leaguePrize, List<String> cards) {
        for (String card : cards)
            leaguePrize.addItem(card, 1);
    }

    private int getTengwarCount(int position) {
        if (position < 5)
            return 5 - position;
        return 0;
    }

    private List<String> getRandomFoil(List<String> list, int count) {
        List<String> result = new LinkedList<>();
        for (String element : list)
            result.add(element + "*");
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result.subList(0, count);
    }

}
