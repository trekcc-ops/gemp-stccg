package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_022_ContinuingMission_Test extends AbstractAtTest {

    @Test
    public void introTwoPlayerGameWithSeedCardsTest() throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        PhysicalCard continuing = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        PhysicalCard attention = builder.addDrawDeckCard("155_021", "Attention All Hands", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();

        seedCard(P1, continuing);
        useGameText(continuing, P1);

        assertTrue(attention.isInPlay());
    }

}