package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfFacilitySeedPhaseProcess extends ST1EGameProcess {
    public ST1EStartOfFacilitySeedPhaseProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(Phase.SEED_FACILITY);
        for (String player : _game.getPlayerIds()) {
            List<PhysicalCard> facilitySeeds = new LinkedList<>(_game.getGameState().getSeedDeck(player));
            for (PhysicalCard card : facilitySeeds) {
                _game.getGameState().removeCardsFromZone(player, Collections.singleton(card));
                _game.getGameState().addCardToZone(_game, card, Zone.HAND);
            }
        }

    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EFacilitySeedPhaseProcess(0, _game);
    }
}