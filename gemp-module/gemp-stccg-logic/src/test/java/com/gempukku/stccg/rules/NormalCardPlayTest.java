package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NormalCardPlayTest extends AbstractAtTest {

    @Test
    public void normalCardPlayTest()
            throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addOutpost(Affiliation.FEDERATION, P1);
        PersonnelCard wallace1 = builder.addCardInHand("101_203", "Darian Wallace", P1, PersonnelCard.class);
        PersonnelCard wallace2 = builder.addCardInHand("101_203", "Darian Wallace", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
        playCard(P1, wallace1);
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, wallace2));
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
    }

}