package com.gempukku.stccg.processes;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.st1e.*;
import com.gempukku.stccg.processes.tribbles.*;

import java.util.Collection;
import java.util.LinkedList;

public class GameProcessDeserializer {

    private enum ProcessType {
        DilemmaSeedPhaseOpponentsMissionsProcess,
        DilemmaSeedPhaseSharedMissionsProcess,
        DilemmaSeedPhaseYourMissionsProcess,
        DoorwaySeedPhaseProcess,
        ST1EEndOfTurnProcess,
        ST1EFacilitySeedPhaseProcess,
        ST1EMissionSeedPhaseProcess,
        ST1EPlayerOrderProcess,
        ST1EPlayPhaseSegmentProcess,
        StartOfTurnGameProcess,
        TribblesBetweenTurnProcess,
        TribblesEndOfRoundGameProcess,
        TribblesPlayerDrawsAndCanPlayProcess,
        TribblesPlayerOrderProcess,
        TribblesPlayerPlaysOrDraws,
        TribblesStartOfRoundGameProcess
    }

    public static GameProcess deserialize(DefaultGame game, JsonNode node) throws CardNotFoundException {

        int consecutivePasses = node.get("consecutivePasses").intValue();
        Collection<String> playersParticipating = new LinkedList<>();
        if (node.has("playersParticipating")) {
            playersParticipating = JsonUtils.toStringArray(node.get("playersParticipating"));
        }
        ProcessType processType = ProcessType.valueOf(node.get("className").textValue());
        boolean isFinished = node.get("isFinished").booleanValue();

        GameProcess result = switch(processType) {
            case DilemmaSeedPhaseOpponentsMissionsProcess ->
                    new DilemmaSeedPhaseOpponentsMissionsProcess(playersParticipating, (ST1EGame) game);
            case DilemmaSeedPhaseSharedMissionsProcess ->
                    new DilemmaSeedPhaseSharedMissionsProcess(playersParticipating, (ST1EGame) game);
            case DilemmaSeedPhaseYourMissionsProcess ->
                    new DilemmaSeedPhaseYourMissionsProcess(playersParticipating, (ST1EGame) game);
            case DoorwaySeedPhaseProcess -> new DoorwaySeedPhaseProcess(playersParticipating, (ST1EGame) game);
            case ST1EEndOfTurnProcess -> new ST1EEndOfTurnProcess((ST1EGame) game);
            case ST1EFacilitySeedPhaseProcess -> new ST1EFacilitySeedPhaseProcess(consecutivePasses, (ST1EGame) game);
            case ST1EMissionSeedPhaseProcess -> new ST1EMissionSeedPhaseProcess(consecutivePasses, (ST1EGame) game);
            case ST1EPlayerOrderProcess -> new ST1EPlayerOrderProcess((ST1EGame) game);
            case ST1EPlayPhaseSegmentProcess -> new ST1EPlayPhaseSegmentProcess((ST1EGame) game);
            case StartOfTurnGameProcess -> new StartOfTurnGameProcess();
            case TribblesBetweenTurnProcess -> new TribblesBetweenTurnsProcess((TribblesGame) game);
            case TribblesEndOfRoundGameProcess -> new TribblesEndOfRoundGameProcess((TribblesGame) game);
            case TribblesPlayerDrawsAndCanPlayProcess -> new TribblesPlayerDrawsAndCanPlayProcess((TribblesGame) game);
            case TribblesPlayerOrderProcess -> new TribblesPlayerOrderProcess((TribblesGame) game);
            case TribblesPlayerPlaysOrDraws -> new TribblesPlayerPlaysOrDraws((TribblesGame) game);
            case TribblesStartOfRoundGameProcess -> new TribblesStartOfRoundGameProcess((TribblesGame) game);
        };

        if (isFinished)
            result.finish();
        return result;

    }

}