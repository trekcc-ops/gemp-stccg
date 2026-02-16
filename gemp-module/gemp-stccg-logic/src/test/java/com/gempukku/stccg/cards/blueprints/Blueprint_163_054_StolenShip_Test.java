package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

public class Blueprint_163_054_StolenShip_Test extends AbstractAtTest {

    private PhysicalCard stolenShip;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addOutpost(Affiliation.ROMULAN, P1);
        stolenShip = builder.addCardInHand("163_054", "Stolen Ship", P1);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void reportTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        playCard(P1, stolenShip);
    }
}