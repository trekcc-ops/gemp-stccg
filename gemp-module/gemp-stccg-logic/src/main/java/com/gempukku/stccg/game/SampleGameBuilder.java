package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.st1e.ST1EFacilitySeedPhaseProcess;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleGameBuilder {

    public static ST1EGame testShipBattleGame(FormatLibrary formatLibrary, int deckSize,
                                              String playerName1, String playerName2,
                                              CardBlueprintLibrary cardLibrary) throws CardNotFoundException,
            InvalidGameOperationException {
        Map<String, CardDeck> decks = new HashMap<>();
        GameFormat format = formatLibrary.get("debug1e");
        CardDeck testDeck = new CardDeck("Test", format);
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "101_104"); // Federation Outpost
        }

        decks.put(playerName1, testDeck);
        decks.put(playerName2, testDeck);

        ST1EGame game = new ST1EGame(format, decks, cardLibrary, GameTimer.GLACIAL_TIMER);
        setupGameState(playerName1, playerName2, game);
        return game;
    }

    private static void setupGameState(String P1, String P2, ST1EGame stGame) throws CardNotFoundException {
        ShipCard attackingShip = (ShipCard) newCardForGame("116_105", P1, stGame); // I.K.S. Lukara (7-7-7)
        ShipCard defendingTarget = (ShipCard) newCardForGame("103_118", P2, stGame); // I.K.S. K'Ratak (6-8-6)
            MissionCard mission = (MissionCard) newCardForGame("101_194", P1, stGame); // Wormhole Negotiations

            PersonnelCard klag1 = (PersonnelCard) newCardForGame("101_270", P1, stGame);
            PersonnelCard klag2 = (PersonnelCard) newCardForGame("101_270", P2, stGame);

            FacilityCard outpost1 = (FacilityCard) newCardForGame("101_105", P1, stGame); // Klingon Outpost
            FacilityCard outpost2 = (FacilityCard) newCardForGame("101_105", P2, stGame); // Klingon Outpost
            List<FacilityCard> outpostsToSeed = List.of(outpost1, outpost2);

            SeedMissionCardAction seedAction = new SeedMissionCardAction(stGame, mission, 0);
            seedAction.processEffect(stGame);

            for (FacilityCard facility : outpostsToSeed) {
                SeedOutpostAction seedOutpostAction = new SeedOutpostAction(stGame, facility);
                seedOutpostAction.setDestination(mission);
                seedOutpostAction.setAffiliation(facility.getAffiliationForCardArt());
                seedOutpostAction.processEffect(stGame);
            }

            ReportCardAction reportCardAction = new ReportCardAction(stGame, klag1, false, outpost1);
            reportCardAction.setAffiliation(Iterables.getOnlyElement(klag1.getAffiliationOptions()));
            reportCardAction.processEffect(stGame);

        reportCardAction = new ReportCardAction(stGame, attackingShip, false, outpost1);
        reportCardAction.setAffiliation(Iterables.getOnlyElement(attackingShip.getAffiliationOptions()));
        reportCardAction.processEffect(stGame);


        ReportCardAction reportCardAction2 = new ReportCardAction(stGame, klag2, false, outpost2);
        reportCardAction2.setAffiliation(Iterables.getOnlyElement(klag2.getAffiliationOptions()));
        reportCardAction2.processEffect(stGame);

        reportCardAction = new ReportCardAction(stGame, defendingTarget, false, outpost2);
        reportCardAction.setAffiliation(Iterables.getOnlyElement(defendingTarget.getAffiliationOptions()));
        reportCardAction.processEffect(stGame);

            stGame.getGameState().initializePlayerOrder(new PlayerOrder(List.of(P1, P2)));
            stGame.getGameState().setCurrentProcess(new ST1EFacilitySeedPhaseProcess(2));

            stGame.startGame();
    }

    private static PhysicalCard newCardForGame(String blueprintId, String playerId, ST1EGame cardGame) throws CardNotFoundException {
        return cardGame.addCardToGame(blueprintId, playerId);
    }

}