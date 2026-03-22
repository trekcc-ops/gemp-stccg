package com.gempukku.stccg.processes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.st1e.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "className")
@JsonSubTypes({@JsonSubTypes.Type(value = DilemmaSeedPhaseOpponentsMissionsProcess.class, name = "DilemmaSeedPhaseOpponentsMissionsProcess"),
        @JsonSubTypes.Type(value = DilemmaSeedPhaseSharedMissionsProcess.class, name = "DilemmaSeedPhaseSharedMissionsProcess"),
        @JsonSubTypes.Type(value = DilemmaSeedPhaseYourMissionsProcess.class, name = "DilemmaSeedPhaseYourMissionsProcess"),
        @JsonSubTypes.Type(value = DoorwaySeedPhaseProcess.class, name = "DoorwaySeedPhaseProcess"),
        @JsonSubTypes.Type(value = ST1EEndOfTurnProcess.class, name = "ST1EEndOfTurnProcess"),
        @JsonSubTypes.Type(value = ST1EFacilitySeedPhaseProcess.class, name = "ST1EFacilitySeedPhaseProcess"),
        @JsonSubTypes.Type(value = ST1EMissionSeedPhaseProcess.class, name = "ST1EMissionSeedPhaseProcess"),
        @JsonSubTypes.Type(value = ST1EPlayerOrderProcess.class, name = "ST1EPlayerOrderProcess"),
        @JsonSubTypes.Type(value = ST1EPlayPhaseSegmentProcess.class, name = "ST1EPlayPhaseSegmentProcess"),
        @JsonSubTypes.Type(value = StartOfTurnGameProcess.class, name = "StartOfTurnGameProcess")
})
public abstract class GameProcess {
    @JsonProperty("consecutivePasses")
    protected int _consecutivePasses;
    private boolean _isFinished;

    protected GameProcess() { }
    protected GameProcess(int consecutivePasses) {
        _consecutivePasses = consecutivePasses;
    }

    public GameProcess(int consecutivePasses, boolean isFinished) {
        _consecutivePasses = consecutivePasses;
        _isFinished = isFinished;
    }

    public abstract void process(DefaultGame cardGame) throws InvalidGameOperationException;

    @JsonIgnore
    public abstract GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException;

    @JsonProperty("isFinished")
    public boolean isFinished() { return _isFinished; }
    @JsonProperty("isFinished")
    public void setFinished(boolean isFinished) {
        _isFinished = isFinished;
    }

    public void continueProcess(DefaultGame cardGame) throws InvalidGameOperationException {
        if (_isFinished) {
            cardGame.getGameState().setCurrentProcess(getNextProcess(cardGame));
        } else {
            process(cardGame);
            _isFinished = true;
        }
    }
}