package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_155_060_Geordi_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private ShipCard runabout;
    private PersonnelCard geordi;
    private MissionCard mission;

    private void initializeGame(MissionType missionType) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = switch(missionType) {
            case SPACE -> builder.addMission("101_171", "Investigate Rogue Comet", P1);
            case PLANET -> builder.addMission("101_154", "Excavation", P1);
            case DUAL -> throw new RuntimeException("Test is not set up for dual missions");
            case HEADQUARTERS -> throw new RuntimeException("Test is not set up to use 2E headquarters missions");
        };
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        geordi = builder.addCardAboardShipOrFacility("155_060", "Geordi La Forge", P1, runabout, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game.startGame();
    }

    @Test
    public void planetSkillsTest() throws CardNotFoundException, InvalidGameOperationException {
        initializeGame(MissionType.PLANET);
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.NAVIGATION));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.ASTROPHYSICS));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.ENGINEER));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.PHYSICS));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.COMPUTER_SKILL));
    }

    @Test
    public void spaceSkillsTest() throws CardNotFoundException, InvalidGameOperationException {
        initializeGame(MissionType.SPACE);

        assertEquals(1, geordi.getSkillLevel(_game, SkillName.NAVIGATION));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.ASTROPHYSICS));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.ENGINEER));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.PHYSICS));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.COMPUTER_SKILL));
    }
}