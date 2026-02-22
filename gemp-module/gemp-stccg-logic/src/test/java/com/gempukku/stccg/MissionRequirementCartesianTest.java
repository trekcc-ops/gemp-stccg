package com.gempukku.stccg;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MissionRequirementCartesianTest extends AbstractAtTest {

    @Test
    public void cartesianTest() throws Exception {
        CardBlueprint tranquilVisit = _cardLibrary.get("155_049");
        MissionRequirement requirement = tranquilVisit.getMissionRequirements();
        List<MissionRequirement> requirementsWithoutOr = requirement.getRequirementOptionsWithoutOr();
        assertEquals("Diplomacy + Anthropology + Jean-Luc Picard", requirementsWithoutOr.get(0).toString());
        assertEquals("Diplomacy + Anthropology + Tebok", requirementsWithoutOr.get(1).toString());
        assertEquals("Diplomacy + Anthropology + CUNNING>35", requirementsWithoutOr.get(2).toString());
    }
}