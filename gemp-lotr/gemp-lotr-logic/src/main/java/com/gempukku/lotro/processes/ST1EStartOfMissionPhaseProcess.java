package com.gempukku.lotro.processes;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.ST1EGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EStartOfMissionPhaseProcess implements GameProcess<ST1EGame> {
    public ST1EStartOfMissionPhaseProcess() {    }

    @Override
    public void process(ST1EGame game) {
        game.getGameState().setCurrentPhase(Phase.SEED_MISSION);
        for (String player : game.getPlayers()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>();
            for (PhysicalCard mission : game.getGameState().getMissionPile(player)) {
                    missionSeeds.add(mission);
            }
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