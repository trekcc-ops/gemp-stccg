package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfMissionPhaseProcess extends ST1EGameProcess {

    ST1EStartOfMissionPhaseProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process() {
        ST1EGameState _gameState = _game.getGameState();
        _gameState.setCurrentPhase(Phase.SEED_MISSION);
        for (String player : _game.getPlayerIds()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>(_gameState.getMissionPile(player));
            Collections.shuffle(missionSeeds);
            for (PhysicalCard card : missionSeeds) {
                _gameState.removeCardsFromZone(player, Collections.singleton(card));
                _gameState.addCardToZone(card, Zone.HAND);
            }
        }

    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EMissionSeedPhaseProcess(0,new ST1EStartOfDilemmaSeedPhaseProcess(_game), _game);
    }
}