package com.gempukku.stccg.game;


import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.processes.TurnProcedure;

/**
 * Defines a snapshot of a game. Since the DefaultGame class is not a snapshotable,
 * this class is used as a starting point to snapshot of all the elements of the game.
 */
public class GameSnapshot implements Snapshotable<GameSnapshot> {
    private int _id;
    private String _description;
    private GameState _gameState;
    private ModifiersLogic _modifiersLogic;
    private ActionsEnvironment _actionsEnvironment;
    private TurnProcedure _turnProcedure;

    /**
     * Creates a game snapshot of the game.
     * @param id the snapshot ID
     * @param description the description
     * @param gameState the game state to snapshot
     * @param modifiersLogic the modifiers logic to snapshot
     * @param actionsEnvironment the actions environment to snapshot
     * @param turnProcedure the turn procedure to snapshot
     * @return the game snapshot
     */
    public static GameSnapshot createGameSnapshot(int id, String description, GameState gameState,
                                                  ModifiersLogic modifiersLogic, ActionsEnvironment actionsEnvironment,
                                                  TurnProcedure turnProcedure) {
        GameSnapshot gameSnapshot =
                new GameSnapshot(id, description, gameState, modifiersLogic, actionsEnvironment, turnProcedure);
        SnapshotData snapshotMetadata = new SnapshotData();
        return snapshotMetadata.getDataForSnapshot(gameSnapshot);
    }

    @Override
    public GameSnapshot generateSnapshot(SnapshotData snapshotData) {
        GameState newGameState;

        if (_gameState instanceof ST1EGameState st1eGameState)
            newGameState = snapshotData.getDataForSnapshot(st1eGameState);
        else
            throw new RuntimeException("blork"); // TODO SNAPSHOT - Get rid of this

        ModifiersLogic newModifiersLogic = snapshotData.getDataForSnapshot(_modifiersLogic);
        ActionsEnvironment newEnvironment = snapshotData.getDataForSnapshot(_actionsEnvironment);
        TurnProcedure newProcedure = snapshotData.getDataForSnapshot(_turnProcedure);


        return new GameSnapshot(_id, _description, newGameState, newModifiersLogic, newEnvironment, newProcedure);
    }


    /**
     * Constructs a game snapshot object that will be used to snapshot all the elements of the game.
     * @param id the snapshot ID
     * @param description the description
     * @param gameState the game state to snapshot
     * @param modifiersLogic the modifiers logic to snapshot
     * @param actionsEnvironment the actions environment to snapshot
     * @param turnProcedure the turn procedure to snapshot
     */
    private GameSnapshot(int id, String description, GameState gameState, ModifiersLogic modifiersLogic,
                         ActionsEnvironment actionsEnvironment, TurnProcedure turnProcedure) {
        _id = id;
        _description = description;
        _gameState = gameState;
        _modifiersLogic = modifiersLogic;
        _actionsEnvironment = actionsEnvironment;
        _turnProcedure = turnProcedure;
    }

    /**
     * Gets the snapshot ID.
     * @return the snapshot ID
     */
    public int getId() {
        return _id;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Gets the current player at time of snapshot.
     * @return the current player at time of snapshot
     */
    public String getCurrentPlayerId() {
        return _gameState.getCurrentPlayerId();
    }

    /**
     * Gets the turn number at time of snapshot.
     * @return the turn number at time of snapshot
     */
    public int getCurrentTurnNumber() {
        return _gameState.getPlayersLatestTurnNumber(getCurrentPlayerId());
    }

    /**
     * Gets the phase at time of snapshot.
     * @return the phase at time of snapshot
     */
    public Phase getCurrentPhase() {
        return _gameState.getCurrentPhase();
    }

    /**
     * Gets the game state.
     * @return the game state
     */
    public GameState getGameState() {
        return _gameState;
    }

    /**
     * Gets the modifiers logic.
     * @return the modifiers logic
     */
    public ModifiersLogic getModifiersLogic() {
        return _modifiersLogic;
    }

    /**
     * Gets the actions environment
     * @return the actions environement
     */
    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    /**
     * Gets the turn procedure.
     * @return the turn procedure
     */
    public TurnProcedure getTurnProcedure() {
        return _turnProcedure;
    }
}