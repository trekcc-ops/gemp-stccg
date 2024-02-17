package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfFacilitySeedPhaseProcess implements GameProcess<ST1EGame> {
    public ST1EStartOfFacilitySeedPhaseProcess() {    }

    @Override
    public void process(ST1EGame game) {
        game.getGameState().setCurrentPhase(Phase.SEED_FACILITY);
        for (String player : game.getPlayerIds()) {
            List<PhysicalCard> facilitySeeds = new LinkedList<>(game.getGameState().getSeedDeck(player));
            for (PhysicalCard card : facilitySeeds) {
                game.getGameState().removeCardsFromZone(player, Collections.singleton(card));
                game.getGameState().addCardToZone(game, card, Zone.HAND);
            }
        }

    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EFacilitySeedPhaseProcess(0);
    }
}