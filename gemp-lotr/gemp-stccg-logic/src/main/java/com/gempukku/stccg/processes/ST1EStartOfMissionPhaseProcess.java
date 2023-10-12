package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfMissionPhaseProcess implements GameProcess<ST1EGame> {
    public ST1EStartOfMissionPhaseProcess() {    }

    @Override
    public void process(ST1EGame game) {
        game.getGameState().setCurrentPhase(Phase.SEED_MISSION);
        for (String player : game.getPlayers()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>(game.getGameState().getMissionPile(player));
            for (PhysicalCard card : missionSeeds) {
                game.getGameState().removeCardsFromZone(player, Collections.singleton(card));
                game.getGameState().addCardToZone(game, card, Zone.HAND);
            }
        }

    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EMissionSeedPhaseProcess(0,new ST1EDoorwaySeedPhaseProcess(null,null));
    }
}