package com.gempukku.lotro.league;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;

public interface LeaguePrizes {
    CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType);

    CardCollection getTrophiesForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed, CollectionType collectionType);
}
