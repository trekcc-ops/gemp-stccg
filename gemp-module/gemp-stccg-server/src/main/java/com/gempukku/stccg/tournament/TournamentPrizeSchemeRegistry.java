package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;

public class TournamentPrizeSchemeRegistry {
    public static TournamentPrizes getTournamentPrizes(CardBlueprintLibrary library, String prizesScheme) {
        return switch(prizesScheme) {
            case null -> new NoPrizes();
            case "none" -> new NoPrizes();
            case "onDemand" -> new SingleEliminationOnDemandPrizes(library, prizesScheme);
            case "daily" -> new DailyTournamentPrizes(library, prizesScheme);
            default -> null;
        };
    }
}