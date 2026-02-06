package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NormalCardPlayTest extends AbstractAtTest {

    @Test
    public void normalCardPlayTest()
            throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("101_154", "Excavation", P1);
        FacilityCard outpost = builder.addFacility("101_104", P1);
        PersonnelCard wallace1 = builder.addCardInHand("101_203", "Darian Wallace", P1, PersonnelCard.class);
        builder.addCardInHand("101_203", "Darian Wallace", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();

        reportCard(P1, wallace1, outpost);

        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
    }

}