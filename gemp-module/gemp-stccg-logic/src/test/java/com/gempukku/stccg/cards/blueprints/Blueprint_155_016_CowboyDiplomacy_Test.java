package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Blueprint_155_016_CowboyDiplomacy_Test extends AbstractAtTest {

    private PersonnelCard picard;
    private PhysicalCard diplomacy;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_154", "Excavation", P2);
        picard = builder.addCardOnPlanetSurface(
                "101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        diplomacy = builder.addCardInHand("155_016", "Cowboy Diplomacy", P1);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void scorePointsTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException {
        initializeGame();
        playCard(P1, diplomacy);
        assertEquals(5, _game.getPlayer(P1).getScore());
        assertFalse(diplomacy.isInPlay());
        assertEquals(Zone.DISCARD, diplomacy.getZone());
    }

}