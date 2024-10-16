package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfDilemmaSeedPhaseProcess extends ST1EGameProcess {
    public ST1EStartOfDilemmaSeedPhaseProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process() {
        ST1EGameState gameState = _game.getGameState();
        gameState.setCurrentPhase(Phase.SEED_DILEMMA);
        for (String player : _game.getPlayerIds()) {
            List<PhysicalCard> remainingSeeds = new LinkedList<>(gameState.getSeedDeck(player));
            for (PhysicalCard card : remainingSeeds) {
                gameState.removeCardsFromZone(player, Collections.singleton(card));
                gameState.addCardToZone(card, Zone.HAND);
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EDilemmaSeedPhaseProcess(new HashSet<>(), _game);
    }
}