package com.gempukku.lotro.league;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroFormat;

public interface LeagueSeriesData {
    int getStart();

    int getEnd();

    int getMaxMatches();

    boolean isLimited();

    String getName();

    LotroFormat getFormat();

    CollectionType getCollectionType();

    CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);
}
