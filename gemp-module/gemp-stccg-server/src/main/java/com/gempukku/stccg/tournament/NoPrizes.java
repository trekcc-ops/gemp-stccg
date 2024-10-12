package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CardCollection;

public class NoPrizes implements TournamentPrizes{
    @Override
    public final CardCollection getPrizeForTournament(PlayerStanding playerStanding) {
        return null;
    }

    @Override
    public final String getRegistryRepresentation() {
        return null;
    }

    @Override
    public final String getPrizeDescription() {
        return "No prizes";
    }
}