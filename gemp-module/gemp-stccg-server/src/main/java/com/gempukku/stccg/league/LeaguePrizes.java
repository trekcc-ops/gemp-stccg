package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.collection.CardCollection;

public interface LeaguePrizes {
    CardCollection getPrizeForLeagueMatchWinner(int winCount, int gamesPlayed);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType);

    CardCollection getTrophiesForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType);
}
