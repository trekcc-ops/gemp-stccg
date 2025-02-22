package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CardCollection;

public interface TournamentPrizes {
    CardCollection getPrizeForTournament(PlayerStanding playerStanding);
    String getRegistryRepresentation();
    String getPrizeDescription();
}