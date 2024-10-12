package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SingleEliminationOnDemandPrizes implements TournamentPrizes{
    private final List<String> _promos = new ArrayList<>();
    private final String _registryRepresentation;

    public SingleEliminationOnDemandPrizes(CardBlueprintLibrary library, String prizesScheme) {
        _registryRepresentation = prizesScheme;
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.hasFlag("originalSet"))
                _promos.addAll(setDefinition.getCardsOfRarity("P"));
        }
    }

    @Override
    public final CardCollection getPrizeForTournament(PlayerStanding playerStanding) {
        DefaultCardCollection tournamentPrize = new DefaultCardCollection();
        if (playerStanding.getPoints() == 4) {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 2);
            tournamentPrize.addItem(getRandom(_promos), 1);
        } else if (playerStanding.getPoints() == 3) {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 2);
        } else {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 1);
        }

        if (!tournamentPrize.getAll().iterator().hasNext())
            return null;
        return tournamentPrize;
    }

    private static String getRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    @Override
    public final String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public final String getPrizeDescription() {
        return "<div class='prizeHint' value='2 wins - 2 boosters and a random promo, 1 win - 2 boosters, 0 wins - 1 booster'>(2+promo)-2-1</div>";
    }
}