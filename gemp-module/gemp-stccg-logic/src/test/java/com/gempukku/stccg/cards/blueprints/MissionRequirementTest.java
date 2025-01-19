package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.common.filterable.CardType;
import org.junit.jupiter.api.Test;

public class MissionRequirementTest extends AbstractAtTest {

    @Test
    public void missionReqTest() {
        for (String blueprintId : _cardLibrary.getAllBlueprintIds()) {
            CardBlueprint blueprint = _cardLibrary.get(blueprintId);
            try {
                if (blueprint.getCardType() == CardType.MISSION) {
                    System.out.println(blueprint.getMissionRequirements().toString());
                }
            } catch(Exception exp) {
                System.out.println("Couldn't parse " + blueprint.getTitle());
            }
        }
    }
}