package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.game.CardCollection;

public interface TournamentPrizes {
    CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount);
    CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount);
    String getRegistryRepresentation();
    String getPrizeDescription();
}
