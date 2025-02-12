package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.common.SetDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DailyTournamentPrizes implements TournamentPrizes {
    private final List<String> _promos = new ArrayList<>();
    private final String _registryRepresentation;

    public DailyTournamentPrizes(CardBlueprintLibrary library, String registryRepresentation) {
        _registryRepresentation = registryRepresentation;
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.hasFlag("originalSet"))
                _promos.addAll(setDefinition.getCardsOfRarity("P"));
        }
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding) {
        DefaultCardCollection tournamentPrize = new DefaultCardCollection();
        tournamentPrize.addItem("(S)All Decipher Choice - Booster", playerStanding.getPoints());
        if (playerStanding.getPlayerWins() + playerStanding.getPlayerByes() >= 2)
            tournamentPrize.addItem(getRandom(_promos), 1);

        if (!tournamentPrize.getAll().iterator().hasNext()) {
            return null;
        }
        return tournamentPrize;
    }

    private String getRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPrizeDescription() {
        return "2 boosters per win (or bye), 1 per loss, max 3 rounds, players with at least 2 wins get a promo";
    }
}