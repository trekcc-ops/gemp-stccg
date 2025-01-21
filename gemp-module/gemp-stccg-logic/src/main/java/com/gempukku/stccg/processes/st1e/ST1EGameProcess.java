package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public abstract class ST1EGameProcess extends GameProcess {

    public ST1EGameProcess() { super(); }
    public ST1EGameProcess(int consecutivePasses) {
        super(consecutivePasses);
    }

    protected ST1EGame getST1EGame(DefaultGame game) throws InvalidGameLogicException {
        if (game instanceof ST1EGame stGame) {
            return stGame;
        } else {
            throw new InvalidGameLogicException("Cannot use a 1E GameProcess with a non-1E game");
        }
    }

}