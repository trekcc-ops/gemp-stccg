package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.collection.CardCollection;

public interface LeaguePrizes {
    CardCollection getPrizeForLeagueMatchWinner(int winCount);

    CardCollection getPrizeForLeague(int position, CollectionType collectionType);

}