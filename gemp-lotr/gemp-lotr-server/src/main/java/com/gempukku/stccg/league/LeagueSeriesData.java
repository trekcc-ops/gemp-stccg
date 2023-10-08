package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.game.CardCollection;
import com.gempukku.stccg.game.GameFormat;

public interface LeagueSeriesData {
    int getStart();

    int getEnd();

    int getMaxMatches();

    boolean isLimited();

    String getName();

    GameFormat getFormat();

    CollectionType getCollectionType();

    CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);
}
