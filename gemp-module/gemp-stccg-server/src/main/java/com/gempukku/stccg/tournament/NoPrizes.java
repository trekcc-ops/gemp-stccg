package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CardCollection;

public class NoPrizes implements TournamentPrizes{
    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount) {
        return null;
    }

    @Override
    public String getRegistryRepresentation() {
        return null;
    }

    @Override
    public String getPrizeDescription() {
        return "No prizes";
    }
}
