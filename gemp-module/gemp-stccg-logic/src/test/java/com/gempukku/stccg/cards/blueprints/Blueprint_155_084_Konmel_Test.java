package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_084_Konmel_Test extends AbstractAtTest {

    @Test
    public void downloadWithYourKorrisTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PersonnelCard konmel =
                builder.addCardOnPlanetSurface("155_084", "Konmel", P1, mission, PersonnelCard.class);
        PersonnelCard korris =
                builder.addCardOnPlanetSurface("155_086", "Korris", P1, mission, PersonnelCard.class);
        PhysicalCard klingonDisruptor =
                builder.addDrawDeckCard("101_058", "Klingon Disruptor", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        downloadCard(P1, klingonDisruptor);
    }

    @Test
    public void downloadWithOpponentsKorrisTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PersonnelCard konmel =
                builder.addCardOnPlanetSurface("155_084", "Konmel", P1, mission, PersonnelCard.class);
        PersonnelCard korris =
                builder.addCardOnPlanetSurface("101_275", "Korris", P2, mission, PersonnelCard.class);
        PhysicalCard klingonDisruptor =
                builder.addDrawDeckCard("101_058", "Klingon Disruptor", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        downloadCard(P1, klingonDisruptor);
    }

    @Test
    public void downloadFailWithNoKorrisTest() throws CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PersonnelCard konmel =
                builder.addCardOnPlanetSurface("155_084", "Konmel", P1, mission, PersonnelCard.class);
        PhysicalCard klingonDisruptor =
                builder.addDrawDeckCard("101_058", "Klingon Disruptor", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        assertThrows(DecisionResultInvalidException.class, () -> downloadCard(P1, klingonDisruptor));
    }

    @Test
    public void downloadFailWithNoDisruptorTest() throws CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PersonnelCard konmel =
                builder.addCardOnPlanetSurface("155_084", "Konmel", P1, mission, PersonnelCard.class);
        PersonnelCard korris =
                builder.addCardOnPlanetSurface("101_275", "Korris", P2, mission, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        assertTrue(getSelectableActionsOfClass(P1, DownloadAction.class).isEmpty());
    }


}